package com.dynamic_survey.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * [教學說明] SurveyStatsDTO (問卷統計資料 DTO)
 * -----------------------------------------------------------------------------
 * 用於回傳問卷的統計結果，包含：
 * 1. 總填寫人數。
 * 2. 每一題的統計數據 (選擇題的選項分佈、簡答題的文字列表)。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SurveyStatsDTO {
    private Long surveyId;
    private String surveyTitle;
    private long totalResponses;
    private List<QuestionStatsDTO> questionStats;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionStatsDTO {
        private Long questionId;
        private String questionTitle;
        private String type; // SINGLE, MULTI, TEXT
        
        // 用於選擇題 (選項ID -> 統計數據)
        private Map<Long, OptionStatsDTO> optionStats;
        
        // 用於簡答題 (列出所有回答)
        private List<String> textAnswers;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionStatsDTO {
        private String optionText;
        private long count;      // 被選次數
        private double percentage; // 百分比
    }
}
