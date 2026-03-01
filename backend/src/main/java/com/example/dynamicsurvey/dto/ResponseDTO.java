package com.example.dynamicsurvey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * [教學說明] 提交問卷的資料傳輸物件 (Response DTO)
 */
@Data
public class ResponseDTO {
    private Long surveyId;

    // === 新增作答者資訊欄位 ===
    @NotBlank(message = "姓名不可為空")
    private String name;

    @NotBlank(message = "手機不可為空")
    private String phone;

    private String email; // 選填

    @NotNull(message = "年齡不可為空")
    private Integer age;

    private List<AnswerDTO> answers;
}
