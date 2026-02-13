package com.dynamic_survey.backend.service;

import com.dynamic_survey.backend.dto.SurveyDTO;
import com.dynamic_survey.backend.entity.Option;
import com.dynamic_survey.backend.entity.Question;
import com.dynamic_survey.backend.entity.Survey;
import com.dynamic_survey.backend.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [教學說明] SurveyService (問卷商業邏輯服務)
 * -----------------------------------------------------------------------------
 * 處理問卷的 CRUD，特別是複雜的一對多連動儲存。
 */
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final com.dynamic_survey.backend.repository.ResponseRepository responseRepository;
    private final com.dynamic_survey.backend.repository.UserRepository userRepository;

    /**
     * [功能] 提交問卷答案
     */
    @Transactional
    public void submitResponse(com.dynamic_survey.backend.dto.ResponseDTO dto, com.dynamic_survey.backend.entity.User user) {
        Survey survey = surveyRepository.findById(dto.getSurveyId())
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        com.dynamic_survey.backend.entity.Response response = com.dynamic_survey.backend.entity.Response.builder()
                .survey(survey)
                .user(user)
                .submittedAt(java.time.LocalDateTime.now())
                .build();

        for (com.dynamic_survey.backend.dto.ResponseDTO.AnswerDTO aDto : dto.getAnswers()) {
            Question question = survey.getQuestions().stream()
                    .filter(q -> q.getId().equals(aDto.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Question not found in this survey"));

            // 處理選擇題答案 (單選或多選)
            if (aDto.getOptionIds() != null && !aDto.getOptionIds().isEmpty()) {
                for (Long optionId : aDto.getOptionIds()) {
                    Option option = question.getOptions().stream()
                            .filter(o -> o.getId().equals(optionId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Option not found in this question"));

                    response.getAnswers().add(com.dynamic_survey.backend.entity.ResponseAnswer.builder()
                            .response(response)
                            .question(question)
                            .option(option)
                            .build());
                }
            } 
            // 處理簡答題答案
            else if (aDto.getAnswerText() != null) {
                response.getAnswers().add(com.dynamic_survey.backend.entity.ResponseAnswer.builder()
                        .response(response)
                        .question(question)
                        .answerText(aDto.getAnswerText())
                        .build());
            }
        }

        responseRepository.save(response);
    }

    /**
     * [功能] 取得所有問卷列表
     */
    public List<SurveyDTO> getAllSurveys() {
        return surveyRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * [功能] 取得問卷統計數據
     */
    @Transactional(readOnly = true)
    public com.dynamic_survey.backend.dto.SurveyStatsDTO getSurveyStats(Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("Survey not found"));

        // 1. 計算總填寫人數 (透過 Response 表)
        // 注意：這裡假設 Survey 與 Response 有建立雙向關聯，如果沒有，可能需要用 Repository 查
        // 為了簡單起見，我們直接用 Repository 查
        List<com.dynamic_survey.backend.entity.Response> responses = responseRepository.findAll().stream()
                .filter(r -> r.getSurvey().getId().equals(surveyId))
                .collect(Collectors.toList());
        
        long totalResponses = responses.size();

        // 2. 遍歷每一個題目進行統計
        List<com.dynamic_survey.backend.dto.SurveyStatsDTO.QuestionStatsDTO> questionStatsList = new java.util.ArrayList<>();

        for (Question question : survey.getQuestions()) {
            com.dynamic_survey.backend.dto.SurveyStatsDTO.QuestionStatsDTO qStats = com.dynamic_survey.backend.dto.SurveyStatsDTO.QuestionStatsDTO.builder()
                    .questionId(question.getId())
                    .questionTitle(question.getTitle())
                    .type(question.getType().name())
                    .build();

            // 從所有 Response 中取出針對此題的 Answers
            List<com.dynamic_survey.backend.entity.ResponseAnswer> answersForQuestion = responses.stream()
                    .flatMap(r -> r.getAnswers().stream())
                    .filter(a -> a.getQuestion().getId().equals(question.getId()))
                    .collect(Collectors.toList());

            if (question.getType() == Question.Type.TEXT) {
                // 處理簡答題：收集所有文字回答
                List<String> texts = answersForQuestion.stream()
                        .map(com.dynamic_survey.backend.entity.ResponseAnswer::getAnswerText)
                        .filter(t -> t != null && !t.isBlank()) // 過濾空白
                        .collect(Collectors.toList());
                qStats.setTextAnswers(texts);
            } else {
                // 處理選擇題：計算每個選項的被選次數
                java.util.Map<Long, com.dynamic_survey.backend.dto.SurveyStatsDTO.OptionStatsDTO> optionMap = new java.util.HashMap<>();
                
                // 初始化所有選項 Count 為 0 (確保沒人選的選項也會顯示)
                for (Option opt : question.getOptions()) {
                    optionMap.put(opt.getId(), com.dynamic_survey.backend.dto.SurveyStatsDTO.OptionStatsDTO.builder()
                            .optionText(opt.getOptionText())
                            .count(0)
                            .percentage(0.0)
                            .build());
                }

                // 累加計數
                for (com.dynamic_survey.backend.entity.ResponseAnswer ans : answersForQuestion) {
                    if (ans.getOption() != null && optionMap.containsKey(ans.getOption().getId())) {
                        com.dynamic_survey.backend.dto.SurveyStatsDTO.OptionStatsDTO stat = optionMap.get(ans.getOption().getId());
                        stat.setCount(stat.getCount() + 1);
                    }
                }

                // 計算百分比
                long totalAnswersForThisQuestion = answersForQuestion.size(); // 注意：多選題這裡分母可能是總票數或總人數，這裡用總票數呈現
                // 如果要用「總填寫人數」當分母，請改用 totalResponses
                
                // 為了直觀，選擇題百分比通常以「該題總回答數」為分母
                if (totalAnswersForThisQuestion > 0) {
                    optionMap.values().forEach(stat -> {
                        stat.setPercentage(Math.round((double) stat.getCount() / totalAnswersForThisQuestion * 1000.0) / 10.0);
                    });
                }

                qStats.setOptionStats(optionMap);
            }
            questionStatsList.add(qStats);
        }

        return com.dynamic_survey.backend.dto.SurveyStatsDTO.builder()
                .surveyId(survey.getId())
                .surveyTitle(survey.getTitle())
                .totalResponses(totalResponses)
                .questionStats(questionStatsList)
                .build();
    }

    /**
     * [功能] 取得所有「進行中」的問卷 (前台使用)
     */
    public List<SurveyDTO> getActiveSurveys() {
        return surveyRepository.findActiveSurveys().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * [功能] 取得使用者的填寫歷史
     */
    public List<com.dynamic_survey.backend.dto.ResponseHistoryDTO> getUserHistory(Long userId) {
        return responseRepository.findByUserId(userId).stream()
                .map(r -> com.dynamic_survey.backend.dto.ResponseHistoryDTO.builder()
                        .responseId(r.getId())
                        .surveyId(r.getSurvey().getId())
                        .surveyTitle(r.getSurvey().getTitle())
                        .submittedAt(r.getSubmittedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * [功能] 根據 ID 取得單一問卷詳情
     */
    public SurveyDTO getSurveyById(Long id) {
        Survey survey = surveyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Survey not found"));
        return convertToDTO(survey);
    }

    /**
     * [功能] 建立或更新問卷
     * [教學重點] @Transactional
     * 確保整個問卷、題目、選項的儲存動作在同一個事務中。若其中一個環節出錯，全部回滾。
     */
    @Transactional
    public SurveyDTO saveSurvey(SurveyDTO dto) {
        Survey survey;
        if (dto.getId() != null) {
            survey = surveyRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Survey not found"));
            // 更新基本資料
            survey.setTitle(dto.getTitle());
            survey.setDescription(dto.getDescription());
            survey.setStartDate(dto.getStartDate());
            survey.setEndDate(dto.getEndDate());
            survey.setStatus(dto.getStatus());
            
            // 為了簡化邏輯，更新時通常會先清空舊題目再加入新題目，或進行複雜的 Merge
            // 這裡採用清空重建策略 (搭配 orphanRemoval=true)
            survey.getQuestions().clear();
        } else {
            survey = new Survey();
            survey.setTitle(dto.getTitle());
            survey.setDescription(dto.getDescription());
            survey.setStartDate(dto.getStartDate());
            survey.setEndDate(dto.getEndDate());
            survey.setStatus(dto.getStatus());
        }

        // 處理題目與選項的關聯綁定
        if (dto.getQuestions() != null) {
            for (SurveyDTO.QuestionDTO qDto : dto.getQuestions()) {
                Question question = Question.builder()
                        .title(qDto.getTitle())
                        .type(qDto.getType())
                        .required(qDto.isRequired())
                        .orderIndex(qDto.getOrderIndex())
                        .survey(survey) // 重要：綁定回 Survey
                        .build();

                if (qDto.getOptions() != null) {
                    for (SurveyDTO.OptionDTO oDto : qDto.getOptions()) {
                        Option option = Option.builder()
                                .optionText(oDto.getOptionText())
                                .orderIndex(oDto.getOrderIndex())
                                .question(question) // 重要：綁定回 Question
                                .build();
                        question.getOptions().add(option);
                    }
                }
                survey.getQuestions().add(question);
            }
        }

        Survey savedSurvey = surveyRepository.save(survey);
        return convertToDTO(savedSurvey);
    }

    /**
     * [功能] 刪除問卷
     */
    @Transactional
    public void deleteSurvey(Long id) {
        surveyRepository.deleteById(id);
    }

    // --- 輔助方法：Entity 轉 DTO ---
    private SurveyDTO convertToDTO(Survey survey) {
        return SurveyDTO.builder()
                .id(survey.getId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .status(survey.getStatus())
                .questions(survey.getQuestions().stream().map(q -> 
                    SurveyDTO.QuestionDTO.builder()
                        .id(q.getId())
                        .title(q.getTitle())
                        .type(q.getType())
                        .required(q.isRequired())
                        .orderIndex(q.getOrderIndex())
                        .options(q.getOptions().stream().map(o -> 
                            SurveyDTO.OptionDTO.builder()
                                .id(o.getId())
                                .optionText(o.getOptionText())
                                .orderIndex(o.getOrderIndex())
                                .build()
                        ).collect(Collectors.toList()))
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
