package com.medicology.learning.dto.request;

import lombok.Data;

@Data
public class AiFeedbackUpdateRequest {
    private String aiExplanation;
    private Boolean isCorrect;
}
