package com.example.dynamicsurvey.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SurveyDTO {
    private Long id;

    @NotBlank(message = "問卷標題不可為空")
    @Size(max = 50, message = "標題長度不可超過 50 字")
    private String title;

    @Size(max = 300, message = "說明長度不可超過 300 字")
    private String description;

    @NotNull(message = "開始日期不可為空")
    private LocalDate startDate;

    @NotNull(message = "結束日期不可為空")
    private LocalDate endDate;

    @NotBlank(message = "狀態不可為空")
    private String status;
    
    private boolean hasResponses;

    @Valid
    @NotNull(message = "題目列表不可為空")
    @Size(min = 1, message = "至少需包含一個題目")
    private List<QuestionDTO> questions;
}
