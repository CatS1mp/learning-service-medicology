package com.medicology.learning.dto.response;

import com.medicology.learning.entity.LessonContentBlockKind;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LessonContentBlockResponse {
    private UUID id;
    private Integer orderIndex;
    private LessonContentBlockKind kind;
    private String payload;
    private UUID assessmentId;
    private UUID questionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
