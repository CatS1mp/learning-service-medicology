package com.medicology.learning.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LessonRequest {
    private UUID sectionId;
    private String name;
    private String description;
    private String slug;
    private Integer orderIndex;
    private Integer estimatedDurationMinutes;
    private String difficultyLevel;
    private Boolean isActive;
    private String content;
    private List<LessonContentBlockRequest> blocks;
}
