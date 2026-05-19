package com.medicology.learning.client.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class AssessmentUserProgressSnapshot {
    private List<ContentOutcomeItem> latestFinalizedByContent;
    private List<InProgressAttemptItem> inProgressAttempts;
    private List<RecentGradedAttemptItem> recentGradedAttempts;

    @Data
    public static class ContentOutcomeItem {
        private UUID contentId;
        private boolean passed;
        private Instant completedAt;
    }

    @Data
    public static class InProgressAttemptItem {
        private UUID contentId;
        private UUID attemptId;
    }

    @Data
    public static class RecentGradedAttemptItem {
        private Instant submittedAt;
        private BigDecimal score;
        private BigDecimal maxScore;
    }
}
