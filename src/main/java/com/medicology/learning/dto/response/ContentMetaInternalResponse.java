package com.medicology.learning.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ContentMetaInternalResponse(UUID id, Integer estimatedDurationMinutes) {}
