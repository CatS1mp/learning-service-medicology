package com.medicology.learning.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class SectionRequest {
    private UUID themeId;
    private String name;
    private String slug;
    private Integer orderIndex;
    private Integer estimatedDurationMinutes;
}
