package com.example.dynamicsurvey.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnswerDTO {
    private Long questionId;
    private List<Long> optionIds;
    private String answerText;
}
