package com.dynamic_survey.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * [教學說明] ResponseHistoryDTO (個人填寫紀錄 DTO)
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseHistoryDTO {
    private Long responseId;
    private Long surveyId;
    private String surveyTitle;
    private LocalDateTime submittedAt;
}
