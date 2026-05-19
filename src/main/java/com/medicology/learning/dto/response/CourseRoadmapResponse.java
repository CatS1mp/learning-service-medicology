package com.medicology.learning.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseRoadmapResponse {
    private String topicTitle;
    private String courseSlug;
    private String courseImageUrl;
    private RoadmapProgressResponse progress;
    private List<RoadmapSectionResponse> sections;
    private RoadmapContinueLessonResponse continueLesson;

    @Data
    @Builder
    public static class RoadmapProgressResponse {
        private int current;
        private int total;
    }

    @Data
    @Builder
    public static class RoadmapSectionResponse {
        private UUID id;
        private String title;
        private List<RoadmapLessonNodeResponse> nodes;
    }

    @Data
    @Builder
    public static class RoadmapLessonNodeResponse {
        private UUID id;
        private String slug;
        private String title;
        private String status;
        private String type;
        private int orderIndex;
        private UUID inProgressAttemptId;
        private String description;
    }

    @Data
    @Builder
    public static class RoadmapContinueLessonResponse {
        private String courseInfo;
        private String title;
        private String description;
        private String contentSlug;
        private UUID inProgressAttemptId;
    }
}
