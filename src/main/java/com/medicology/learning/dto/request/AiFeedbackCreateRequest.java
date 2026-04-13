package com.medicology.learning.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class AiFeedbackCreateRequest {
    @NotNull
    private UUID referenceId;
    private String referenceType;
    private String questionContent;
    private String userAnswer;
    private Boolean isCorrect;
}
