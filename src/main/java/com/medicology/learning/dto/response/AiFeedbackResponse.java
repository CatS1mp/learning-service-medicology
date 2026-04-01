package com.medicology.learning.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AiFeedbackResponse {
    private UUID id;
    private UUID userId;
    private UUID referenceId;
    private String referenceType;
    private String questionContent;
    private String userAnswer;
    private Boolean isCorrect;
    private String aiExplanation;
    private LocalDateTime createdAt;
}
