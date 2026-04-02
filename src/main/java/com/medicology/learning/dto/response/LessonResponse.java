package com.medicology.learning.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LessonResponse {
    private UUID id;
    private UUID sectionId;
    private String name;
    private String description;
    private String slug;
    private Integer orderIndex;
    private Integer estimatedDurationMinutes;
    private String difficultyLevel;
    private Boolean isActive;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
