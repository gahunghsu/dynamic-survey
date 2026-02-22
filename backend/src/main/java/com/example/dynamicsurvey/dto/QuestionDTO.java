package com.example.dynamicsurvey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long id;

    @NotBlank(message = "題目名稱不可為空")
    @Size(max = 75, message = "題目不可超過 75 字")
    private String title;

    @NotBlank(message = "題目類型不可為空")
    private String type; // SINGLE, MULTI, TEXT

    private boolean required;

    private int orderIndex;

    private List<OptionDTO> options;
}
