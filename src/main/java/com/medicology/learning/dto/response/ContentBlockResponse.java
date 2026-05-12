package com.medicology.learning.dto.response;

import com.medicology.learning.entity.ContentBlockKind;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContentBlockResponse {
    private UUID id;
    private Integer orderIndex;
    private ContentBlockKind kind;
    private String payload;
    private Integer maxScore;
    private Boolean isGradable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
