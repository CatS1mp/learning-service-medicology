package com.medicology.learning.dto.response;

import com.medicology.learning.entity.ContentBlockKind;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ContentBlockTemplateResponse(
        UUID id,
        ContentBlockKind kind,
        String name,
        String description,
        String payloadSchemaJson,
        String starterPayloadJson,
        Boolean defaultIsGradable,
        Integer defaultMaxScore,
        Boolean allowScoreEdit,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
