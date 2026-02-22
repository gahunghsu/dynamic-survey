package com.example.dynamicsurvey.service;

import com.example.dynamicsurvey.dto.*;
import com.example.dynamicsurvey.entity.*;
import com.example.dynamicsurvey.repository.*;
import com.example.dynamicsurvey.security.UserDetailsImpl;
import com.example.dynamicsurvey.vo.AppResponse;
import com.example.dynamicsurvey.vo.RspCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * [教學說明] 問卷核心業務邏輯層 (Survey Service)
 * -----------------------------------------------------------------------------
 * 【設計意圖】
 * Service 層是系統的「大腦」，負責協調 Controller 傳入的請求與 Repository 的資料操作。
 * 在此專案中，它處理了最複雜的「階層式資料存取」與「統計數據聚合」。
 *
 * 【核心技術】
 * 1. DTO 與 Entity 的轉換：確保資料庫結構 (Entity) 不會直接暴露給前端。
 * 2. 事務管理 (@Transactional)：確保一連串的資料庫操作「要麼全部成功，要麼全部失敗」。
 * 3. Java Stream API：高效處理集合資料與統計計算。
 */
@Service
public class SurveyService {

    @Autowired
    SurveyRepository surveyRepository;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    SurveyResponseRepository responseRepository;

    // =========================================================================
    // 第一部分：前台流程 (User Flow)
    // =========================================================================

    /**
     * [功能] 取得所有進行中的問卷
     * 【關鍵點】調用 Repository 的自定義查詢，僅回傳符合日期範圍且已發佈的問卷。
     */
    public AppResponse<List<SurveyDTO>> getActiveSurveys() {
        List<Survey> surveys = surveyRepository.findActiveSurveys(LocalDate.now());
        return AppResponse.success(surveys.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    /**
     * [功能] 取得單一問卷詳情 (填寫用)
     */
    public AppResponse<SurveyDTO> getSurveyDetails(Long id) {
        return surveyRepository.findById(id)
                .map(s -> AppResponse.success(convertToDTO(s)))
                .orElse(AppResponse.error(RspCode.NOT_FOUND));
    }

    /**
     * [功能] 提交問卷答案
     * -------------------------------------------------------------------------
     * 【實作細節】
     * 1. 從 SecurityContext 取得目前登入的用戶 ID。
     * 2. 遍歷前端傳來的答案列表 (Submission)，根據問題類型 (TEXT/SINGLE/MULTI) 進行處理。
     * 3. 將 Answer 與 SurveyResponse 建立關聯，並一次性透過 JPA Cascade 儲存。
     */
    @Transactional
    public AppResponse<?> submitResponse(Long surveyId, ResponseDTO submission) {
        // 驗證問卷是否存在
        Survey survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null) return AppResponse.error(RspCode.NOT_FOUND);

        // 獲取當前作答者資訊
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);

        // 建立作答主表紀錄
        SurveyResponse response = new SurveyResponse();
        response.setSurvey(survey);
        response.setUser(user);
        response.setSubmittedAt(LocalDateTime.now());

        // 處理每一題的答案
        for (AnswerDTO aDto : submission.getAnswers()) {
            ResponseAnswer answer = new ResponseAnswer();
            answer.setSurveyResponse(response);
            
            // 找出對應的題目實體
            Question question = survey.getQuestions().stream()
                    .filter(q -> q.getId().equals(aDto.getQuestionId()))
                    .findFirst().orElse(null);
            
            if (question == null) continue;
            answer.setQuestion(question);

            // 根據題目類型儲存資料
            if (question.getType().equals("TEXT")) {
                // 簡答題：直接存入字串
                answer.setAnswerText(aDto.getAnswerText());
            } else {
                // 選擇題：將前端傳來的選項 ID 列表轉換為 Entity 列表
                List<Option> selected = question.getOptions().stream()
                        .filter(o -> aDto.getOptionIds().contains(o.getId()))
                        .collect(Collectors.toList());
                answer.setSelectedOptions(selected);
            }
            response.getAnswers().add(answer);
        }

        // 儲存所有紀錄
        responseRepository.save(response);
        return AppResponse.success(null);
    }

    /**
     * [功能] 取得當前使用者的填寫歷史
     */
    public AppResponse<?> getUserHistory() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
    // 第二部分：後台管理 (Admin Methods)
    // =========================================================================

    /**
     * [功能] 搜尋問卷清單 (管理員專用)
     * 【關鍵點】回傳時檢查是否有作答紀錄 (hasResponses)，若有紀錄則前端應禁止刪除。
     */
    public AppResponse<List<SurveyDTO>> getSurveysByAdmin(String title, LocalDate start, LocalDate end) {
        List<Survey> surveys = surveyRepository.findByFilters(title, start, end);
        return AppResponse.success(surveys.stream().map(s -> {
            SurveyDTO dto = convertToDTO(s);
            dto.setHasResponses(responseRepository.existsBySurveyId(s.getId()));
            return dto;
        }).collect(Collectors.toList()));
    }

    /**
     * [功能] 儲存或更新問卷 (最核心邏輯)
     * -------------------------------------------------------------------------
     * 【實作亮點：雙向關聯與階層更新】
     * 1. 判斷模式：透過 DTO 是否帶有 ID 來決定執行 Insert 或 Update。
     * 2. 暴力更新策略：在更新問卷時，我們先清空 (clear) 舊有的題目列表，再重新填充新題目。
     *    配合 CascadeType.ALL 與 orphanRemoval = true，JPA 會自動幫我們刪除舊題目並建立新題目。
     * 3. 雙向關聯 (Bidirectional)：在 Question 物件中必須 setSurvey(survey)，
     *    否則資料庫中的外鍵 (survey_id) 會是 NULL。
     */
    @Transactional
    public AppResponse<SurveyDTO> saveSurvey(SurveyDTO dto) {
        Survey survey = (dto.getId() != null) ? 
                surveyRepository.findById(dto.getId()).orElse(new Survey()) : new Survey();
        
        survey.setTitle(dto.getTitle());
        survey.setDescription(dto.getDescription());
        survey.setStartDate(dto.getStartDate());
        survey.setEndDate(dto.getEndDate());
        survey.setStatus(dto.getStatus());

        // 清空現有題目並根據 DTO 重新建立 (實作階層式儲存)
        survey.getQuestions().clear();
        for (QuestionDTO qDto : dto.getQuestions()) {
            Question q = new Question();
            q.setSurvey(survey); // 重要：建立子對父的關聯
            q.setTitle(qDto.getTitle());
            q.setType(qDto.getType());
            q.setRequired(qDto.isRequired());
            q.setOrderIndex(qDto.getOrderIndex());
            
            // 處理選項
            if (qDto.getOptions() != null) {
                for (OptionDTO oDto : qDto.getOptions()) {
                    Option o = new Option();
                    o.setQuestion(q); // 重要：建立子對父的關聯
                    o.setOptionText(oDto.getOptionText());
                    o.setOrderIndex(oDto.getOrderIndex());
                    q.getOptions().add(o);
                }
            }
            survey.getQuestions().add(q);
        }

        // 保存 Survey 主實體，其下的 Question 與 Option 會連動儲存 (Cascade)
        Survey saved = surveyRepository.save(survey);
        return AppResponse.success(convertToDTO(saved));
    }

    /**
     * [功能] 刪除問卷
     * 【安全性防呆】若該問卷已有使用者作答，基於資料完整性，系統應禁止刪除。
     */
    @Transactional
    public AppResponse<?> deleteSurvey(Long id) {
        if (responseRepository.existsBySurveyId(id)) {
            return AppResponse.error(RspCode.PARAM_ERROR, "已有作答紀錄，無法刪除");
        }
        surveyRepository.deleteById(id);
        return AppResponse.success(null);
    }

    /**
     * [功能] 取得該問卷的所有填寫者清單 (管理員專用)
     * -------------------------------------------------------------------------
     * 【設計意圖】
     * 讓管理員知道有哪些使用者填寫了這份問卷，並顯示基本的作答資訊。
     */
    public AppResponse<?> getSurveyResponses(Long id) {
        // 1. 從資料庫抓取該問卷的所有回覆紀錄
        List<SurveyResponse> responses = responseRepository.findBySurveyId(id);
        
        // 2. 將回覆實體轉換為簡化的 Map 格式回傳
        return AppResponse.success(responses.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("responseId", r.getId());
            map.put("userName", r.getUser().getName());
            map.put("userEmail", r.getUser().getEmail());
            map.put("submittedAt", r.getSubmittedAt());
            return map;
        }).collect(Collectors.toList()));
    }

    /**
     * [功能] 取得問卷統計數據 (數據分析核心)
     * -------------------------------------------------------------------------
     * 【運算邏輯】
     * 1. 抓取該問卷所有的作答紀錄 (SurveyResponses)。
     * 2. 針對每一題：
     *    - 若為簡答題：收集所有的文字回答並過濾掉 NULL 值。
     *    - 若為選擇題：計算每個選項被勾選的次數，並算出百分比 (四捨五入至小數點第一位)。
     * 3. 使用 Stream API 進行扁平化處理 (flatMap)，快速從 Responses 提取 Answers。
     */
    public AppResponse<?> getSurveyStats(Long id) {
        Survey survey = surveyRepository.findById(id).orElse(null);
        if (survey == null) return AppResponse.error(RspCode.NOT_FOUND);

        List<SurveyResponse> responses = responseRepository.findBySurveyId(id);
        int totalResponses = responses.size(); // 總填寫人數
        
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
                // 提取簡答內容清單
                List<String> answers = responses.stream()
                        .flatMap(r -> r.getAnswers().stream())
                        .filter(a -> a.getQuestion().getId().equals(q.getId()))
                        .map(ResponseAnswer::getAnswerText)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                qMap.put("textAnswers", answers);
            } else {
                // 初始化選項統計 Map (選項ID -> 統計數據)
                Map<Long, Map<String, Object>> optStatsMap = new HashMap<>();
                for (Option o : q.getOptions()) {
                    Map<String, Object> oData = new HashMap<>();
                    oData.put("optionText", o.getOptionText());
                    oData.put("count", 0);
                    optStatsMap.put(o.getId(), oData);
                }

                // 累加計數
                responses.stream()
                        .flatMap(r -> r.getAnswers().stream())
                        .filter(a -> a.getQuestion().getId().equals(q.getId()))
                        .flatMap(a -> a.getSelectedOptions().stream())
                        .forEach(o -> {
                            Map<String, Object> oData = optStatsMap.get(o.getId());
                            if (oData != null) {
                                oData.put("count", (int) oData.get("count") + 1);
                            }
                        });

                // 計算百分比
                for (Map<String, Object> oData : optStatsMap.values()) {
                    int count = (int) oData.get("count");
                    double percentage = totalResponses > 0 ? (count * 100.0 / totalResponses) : 0;
                    oData.put("percentage", Math.round(percentage * 10.0) / 10.0);
                }
                qMap.put("optionStats", optStatsMap);
            }
            qStatsList.add(qMap);
        }
        stats.put("questionStats", qStatsList);

        return AppResponse.success(stats);
    }

    /**
     * [工具方法] Entity 轉 DTO
     * 【關鍵點】解耦 Entity (資料庫實體) 與 DTO (前端資料格式)。
     * 避免直接回傳 Entity 導致的延遲加載 (Lazy Initialization) 異常或敏感欄位外洩。
     */
    private SurveyDTO convertToDTO(Survey s) {
        SurveyDTO dto = new SurveyDTO();
        dto.setId(s.getId());
        dto.setTitle(s.getTitle());
        dto.setDescription(s.getDescription());
        dto.setStartDate(s.getStartDate());
        dto.setEndDate(s.getEndDate());
        dto.setStatus(s.getStatus());
        
        dto.setQuestions(s.getQuestions().stream().map(q -> {
            QuestionDTO qDto = new QuestionDTO();
            qDto.setId(q.getId());
            qDto.setTitle(q.getTitle());
            qDto.setType(q.getType());
            qDto.setRequired(q.isRequired());
            qDto.setOrderIndex(q.getOrderIndex());
            
            qDto.setOptions(q.getOptions().stream().map(o -> {
                OptionDTO oDto = new OptionDTO();
                oDto.setId(o.getId());
                oDto.setOptionText(o.getOptionText());
                oDto.setOrderIndex(o.getOrderIndex());
                return oDto;
            }).collect(Collectors.toList()));
            
            return qDto;
        }).collect(Collectors.toList()));
        
        return dto;
    }
}
