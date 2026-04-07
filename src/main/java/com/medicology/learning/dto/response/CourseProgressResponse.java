package com.medicology.learning.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CourseProgressResponse {
    private UUID courseId;
    private String courseName;
    private String courseSlug;
    private LocalDateTime lastStudiedAt;
    private Integer completionPercent;
}
