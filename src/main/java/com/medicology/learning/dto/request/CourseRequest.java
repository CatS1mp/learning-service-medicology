package com.medicology.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "Course name is required")
    @Size(max = 200, message = "Course name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Course slug is required")
    @Size(max = 300, message = "Course slug must not exceed 300 characters")
    private String slug;

    @Size(max = 1000, message = "Course description must not exceed 1000 characters")
    private String description;

    private String iconFileName;

    @Size(max = 20, message = "Course color code must not exceed 20 characters")
    private String colorCode;
}
