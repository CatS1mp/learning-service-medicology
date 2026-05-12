package com.medicology.learning.dto.response;

import com.medicology.learning.entity.ContentBlockKind;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ContentBlockInternalResponse(
        UUID id,
        UUID contentId,
        ContentBlockKind kind,
        String payload,
        Integer maxScore,
        Boolean isGradable,
        Integer orderIndex
) {}
