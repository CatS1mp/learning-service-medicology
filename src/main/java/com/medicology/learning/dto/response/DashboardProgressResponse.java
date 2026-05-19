package com.medicology.learning.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardProgressResponse {
    private List<CourseProgressResponse> courses;
    private ContentActivitySummaryResponse activity;
    private List<RecentGradedAttemptResponse> recentGradedAttempts;
    private double averageScoreOnTenScale;

    @Data
    @Builder
    public static class RecentGradedAttemptResponse {
        private String submittedAt;
        private double scoreOnTenScale;
    }
}
