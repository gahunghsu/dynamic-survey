package com.example.dynamicsurvey.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResponseDTO {
    private Long surveyId;
    private List<AnswerDTO> answers;
}
