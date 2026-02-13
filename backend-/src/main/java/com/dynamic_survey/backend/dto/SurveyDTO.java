package com.dynamic_survey.backend.dto;

import com.dynamic_survey.backend.entity.Question;
import com.dynamic_survey.backend.entity.Survey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * [教學說明] SurveyDTO (問卷傳輸物件)
 * -----------------------------------------------------------------------------
 * 用於封裝整份問卷的完整結構，包含所有題目與選項。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SurveyDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Survey.Status status;
    private List<QuestionDTO> questions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionDTO {
        private Long id;
        private String title;
        private Question.Type type;
        private boolean required;
        private int orderIndex;
        private List<OptionDTO> options;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionDTO {
        private Long id;
        private String optionText;
        private int orderIndex;
    }
}
