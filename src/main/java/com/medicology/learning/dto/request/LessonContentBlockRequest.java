package com.medicology.learning.dto.request;

import com.medicology.learning.entity.LessonContentBlockKind;
import lombok.Data;

import java.util.UUID;

@Data
public class LessonContentBlockRequest {
    private Integer orderIndex;
    private LessonContentBlockKind kind;
    private String payload;
    private UUID assessmentId;
    private UUID questionId;
}
