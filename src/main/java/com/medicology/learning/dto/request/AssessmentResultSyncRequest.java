package com.medicology.learning.dto.request;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssessmentResultSyncRequest(
        UUID userId,
        UUID courseId,
        UUID sectionId,
        UUID assessmentId,
        UUID attemptId,
        BigDecimal score,
        Boolean passed,
        String resultStatus,
        Instant completedAt
) {}
