package com.medicology.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class SectionRequest {
    @NotNull(message = "courseId không được để trống")
    private UUID courseId;
    @NotBlank(message = "Tên section không được để trống")
    private String name;
    private String slug;
    private Integer orderIndex;
    private Integer estimatedDurationMinutes;
}
