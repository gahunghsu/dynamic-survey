package com.dynamic_survey.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * [教學說明] ResponseDTO (提交答案用)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    private Long surveyId;
    private List<AnswerDTO> answers;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnswerDTO {
        private Long questionId;
        private List<Long> optionIds; // 多選題可能有多個 ID
        private String answerText;    // 簡答題內容
    }
}
