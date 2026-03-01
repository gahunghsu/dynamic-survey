package com.example.dynamicsurvey.service;

import com.example.dynamicsurvey.dto.*;
import com.example.dynamicsurvey.entity.*;
import com.example.dynamicsurvey.repository.*;
import com.example.dynamicsurvey.security.UserDetailsImpl;
import com.example.dynamicsurvey.vo.AppResponse;
import com.example.dynamicsurvey.vo.RspCode;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * [教學說明] 問卷核心業務邏輯層 (Survey Service)
 */
@Service
public class SurveyService {

    @Autowired
    SurveyRepository surveyRepository;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    SurveyResponseRepository responseRepository;

    // 前台作答 Session Key
    private static final String SURVEY_SESSION_KEY = "TEMP_SURVEY_RESPONSE";
    // 後台編輯 Session Key
    private static final String ADMIN_EDIT_SESSION_KEY = "TEMP_ADMIN_SURVEY";

    // =========================================================================
    // 第一部分：前台作答流程 (略，維持不變)
    // =========================================================================

    public AppResponse<List<SurveyDTO>> getActiveSurveys() {
        List<Survey> surveys = surveyRepository.findActiveSurveys();
        return AppResponse.success(surveys.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    public AppResponse<SurveyDTO> getSurveyDetails(Long id) {
        return surveyRepository.findById(id).map(s -> AppResponse.success(convertToDTO(s))).orElse(AppResponse.error(RspCode.NOT_FOUND));
    }

    public AppResponse<?> saveToSession(ResponseDTO submission, HttpSession session) {
        if (responseRepository.existsBySurveyIdAndEmail(submission.getSurveyId(), submission.getEmail())) {
            return AppResponse.error(RspCode.DUPLICATE_ERROR, "此 Email 已填寫過本問卷。");
        }
        session.setAttribute(SURVEY_SESSION_KEY, submission);
        return AppResponse.success(null);
    }

    public AppResponse<ResponseDTO> getFromSession(HttpSession session) {
        ResponseDTO data = (ResponseDTO) session.getAttribute(SURVEY_SESSION_KEY);
        if (data == null) return AppResponse.error(RspCode.NOT_FOUND);
        return AppResponse.success(data);
    }

    @Transactional
    public AppResponse<?> commitFromSession(HttpSession session) {
        ResponseDTO submission = (ResponseDTO) session.getAttribute(SURVEY_SESSION_KEY);
        if (submission == null) return AppResponse.error(RspCode.NOT_FOUND);
        AppResponse<?> response = submitResponse(submission.getSurveyId(), submission);
        if (response.getCode() == 200) session.removeAttribute(SURVEY_SESSION_KEY);
        return response;
    }

    @Transactional
    public AppResponse<?> submitResponse(Long surveyId, ResponseDTO submission) {
        Survey survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null) return AppResponse.error(RspCode.NOT_FOUND);
        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setSubmittedAt(LocalDateTime.now());
        response.setName(submission.getName());
        response.setPhone(submission.getPhone());
        response.setEmail(submission.getEmail());
        response.setAge(submission.getAge());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            User user = userRepository.findById(userDetails.getId()).orElse(null);
            response.setUser(user);
        }
        for (AnswerDTO aDto : submission.getAnswers()) {
            ResponseAnswer answer = new ResponseAnswer();
            answer.setSurveyResponse(response);
            Question question = survey.getQuestions().stream().filter(q -> q.getId().equals(aDto.getQuestionId())).findFirst().orElse(null);
            if (question == null) continue;
            answer.setQuestion(question);
            if (question.getType().equals("TEXT")) {
                answer.setAnswerText(aDto.getAnswerText());
            } else {
                List<Option> selected = question.getOptions().stream().filter(o -> aDto.getOptionIds().contains(o.getId())).collect(Collectors.toList());
                answer.setSelectedOptions(selected);
                answer.setAnswerText(selected.stream().map(Option::getOptionText).collect(Collectors.joining(";")));
            }
            response.getAnswers().add(answer);
        }
        responseRepository.save(response);
        return AppResponse.success(null);
    }

    public AppResponse<?> getUserHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) return AppResponse.error(RspCode.UNAUTHORIZED);
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        List<SurveyResponse> history = responseRepository.findByUserOrderBySubmittedAtDesc(user);
        return AppResponse.success(history.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("surveyId", r.getSurvey().getId());
            map.put("surveyTitle", r.getSurvey().getTitle());
            map.put("submittedAt", r.getSubmittedAt());
            return map;
        }).collect(Collectors.toList()));
    }

    // =========================================================================
    // 第三部分：後台管理 (Admin Methods)
    // =========================================================================

    /**
     * [功能] 管理員編輯問卷暫存至 Session
     */
    public AppResponse<?> saveAdminSurveyToSession(SurveyDTO surveyDTO, HttpSession session) {
        session.setAttribute(ADMIN_EDIT_SESSION_KEY, surveyDTO);
        return AppResponse.success(null);
    }

    /**
     * [功能] 管理員從 Session 取得正在編輯的問卷
     */
    public AppResponse<SurveyDTO> getAdminSurveyFromSession(HttpSession session) {
        SurveyDTO dto = (SurveyDTO) session.getAttribute(ADMIN_EDIT_SESSION_KEY);
        if (dto == null) return AppResponse.error(RspCode.NOT_FOUND, "找不到編輯中的資料");
        return AppResponse.success(dto);
    }

    /**
     * [功能] 管理員正式提交問卷並清空 Session
     * @param isPublish 是否發佈 (true -> PUBLISHED, false -> DRAFT)
     */
    @Transactional
    public AppResponse<SurveyDTO> commitAdminSurveyFromSession(boolean isPublish, HttpSession session) {
        SurveyDTO dto = (SurveyDTO) session.getAttribute(ADMIN_EDIT_SESSION_KEY);
        if (dto == null) return AppResponse.error(RspCode.NOT_FOUND);

        // 根據按鈕決定狀態
        dto.setStatus(isPublish ? "PUBLISHED" : "DRAFT");
        
        AppResponse<SurveyDTO> response = saveSurvey(dto);
        if (response.getCode() == 200) {
            session.removeAttribute(ADMIN_EDIT_SESSION_KEY);
        }
        return response;
    }

    // 原有的查詢與儲存核心邏輯
    public AppResponse<List<SurveyDTO>> getSurveysByAdmin(String title, LocalDate start, LocalDate end) {
        List<Survey> surveys = surveyRepository.findByFilters(title, start, end);
        return AppResponse.success(surveys.stream().map(s -> {
            SurveyDTO dto = convertToDTO(s);
            dto.setHasResponses(responseRepository.existsBySurveyId(s.getId()));
            return dto;
        }).collect(Collectors.toList()));
    }

    @Transactional
    public AppResponse<SurveyDTO> saveSurvey(SurveyDTO dto) {
        Survey survey = (dto.getId() != null) ? surveyRepository.findById(dto.getId()).orElse(new Survey()) : new Survey();
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());
        survey.setStartDate(dto.getStartDate());
        survey.setEndDate(dto.getEndDate());
        survey.setStatus(dto.getStatus());
        survey.getQuestions().clear();
        for (QuestionDTO qDto : dto.getQuestions()) {
            Question q = new Question();
            q.setSurvey(survey);
            q.setTitle(qDto.getTitle());
            q.setType(qDto.getType());
            q.setRequired(qDto.isRequired());
            q.setOrderIndex(qDto.getOrderIndex());
            if (qDto.getOptions() != null) {
                for (OptionDTO oDto : qDto.getOptions()) {
                    Option o = new Option();
                    o.setQuestion(q);
                    o.setOptionText(oDto.getOptionText());
                    o.setOrderIndex(oDto.getOrderIndex());
                    q.getOptions().add(o);
                }
            }
            survey.getQuestions().add(q);
        }
        return AppResponse.success(convertToDTO(surveyRepository.save(survey)));
    }

    @Transactional
    public AppResponse<?> deleteSurvey(Long id) {
        if (responseRepository.existsBySurveyId(id)) return AppResponse.error(RspCode.PARAM_ERROR, "已有作答紀錄");
        surveyRepository.deleteById(id);
        return AppResponse.success(null);
    }

    public AppResponse<?> getSurveyResponses(Long id) {
        List<SurveyResponse> responses = responseRepository.findBySurveyIdOrderByIdDesc(id);
        return AppResponse.success(responses.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("responseId", r.getId());
            map.put("userName", r.getName());
            map.put("userEmail", r.getEmail());
            map.put("submittedAt", r.getSubmittedAt());
            return map;
        }).collect(Collectors.toList()));
    }

    public AppResponse<?> getResponseDetail(Long responseId) {
        SurveyResponse response = responseRepository.findById(responseId).orElse(null);
        if (response == null) return AppResponse.error(RspCode.NOT_FOUND);
        Map<String, Object> result = new HashMap<>();
        result.put("responseId", response.getId());
        result.put("userName", response.getName());
        result.put("submittedAt", response.getSubmittedAt());
        result.put("surveyTitle", response.getSurvey().getTitle());
        List<Map<String, Object>> details = response.getAnswers().stream().map(a -> {
            Map<String, Object> aMap = new HashMap<>();
            aMap.put("questionTitle", a.getQuestion().getTitle());
            aMap.put("type", a.getQuestion().getType());
            aMap.put("answer", a.getAnswerText());
            return aMap;
        }).collect(Collectors.toList());
        result.put("details", details);
        return AppResponse.success(result);
    }

    public AppResponse<?> getSurveyStats(Long id) {
        Survey survey = surveyRepository.findById(id).orElse(null);
        if (survey == null) return AppResponse.error(RspCode.NOT_FOUND);
        List<SurveyResponse> responses = responseRepository.findBySurveyId(id);
        int totalResponses = responses.size();
        Map<String, Object> stats = new HashMap<>();
        stats.put("surveyId", survey.getId());
        stats.put("surveyTitle", survey.getTitle());
        stats.put("totalResponses", totalResponses);
        List<Map<String, Object>> qStatsList = new ArrayList<>();
        for (Question q : survey.getQuestions()) {
            Map<String, Object> qMap = new HashMap<>();
            qMap.put("questionId", q.getId());
            qMap.put("questionTitle", q.getTitle());
            qMap.put("type", q.getType());
            if (q.getType().equals("TEXT")) {
                qMap.put("textAnswers", responses.stream().flatMap(r -> r.getAnswers().stream())
                        .filter(a -> a.getQuestion().getId().equals(q.getId())).map(ResponseAnswer::getAnswerText)
                        .filter(Objects::nonNull).collect(Collectors.toList()));
            } else {
                Map<Long, Map<String, Object>> optMap = new HashMap<>();
                for (Option o : q.getOptions()) {
                    Map<String, Object> oData = new HashMap<>();
                    oData.put("optionText", o.getOptionText());
                    oData.put("count", 0);
                    optMap.put(o.getId(), oData);
                }
                responses.stream().flatMap(r -> r.getAnswers().stream())
                        .filter(a -> a.getQuestion().getId().equals(q.getId()))
                        .flatMap(a -> a.getSelectedOptions().stream())
                        .forEach(o -> {
                            Map<String, Object> oData = optMap.get(o.getId());
                            if (oData != null) oData.put("count", (int) oData.get("count") + 1);
                        });
                for (Map<String, Object> oData : optMap.values()) {
                    double pct = totalResponses > 0 ? ((int) oData.get("count") * 100.0 / totalResponses) : 0;
                    oData.put("percentage", Math.round(pct * 10.0) / 10.0);
                }
                qMap.put("optionStats", optMap);
            }
            qStatsList.add(qMap);
        }
        stats.put("questionStats", qStatsList);
        return AppResponse.success(stats);
    }

    private SurveyDTO convertToDTO(Survey s) {
        SurveyDTO dto = new SurveyDTO();
        dto.setId(s.getId()); dto.setTitle(s.getTitle()); dto.setDescription(s.getDescription());
        dto.setStartDate(s.getStartDate()); dto.setEndDate(s.getEndDate()); dto.setStatus(s.getStatus());
        dto.setQuestions(s.getQuestions().stream().map(q -> {
            QuestionDTO qDto = new QuestionDTO();
            qDto.setId(q.getId()); qDto.setTitle(q.getTitle()); qDto.setType(q.getType());
            qDto.setRequired(q.isRequired()); qDto.setOrderIndex(q.getOrderIndex());
            qDto.setOptions(q.getOptions().stream().map(o -> {
                OptionDTO oDto = new OptionDTO();
                oDto.setId(o.getId()); oDto.setOptionText(o.getOptionText()); oDto.setOrderIndex(o.getOrderIndex());
                return oDto;
            }).collect(Collectors.toList()));
            return qDto;
        }).collect(Collectors.toList()));
        return dto;
    }
}
