package com.medicology.learning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "CreateCourseMultipartRequest", description = "Multipart payload for creating a course")
public class CreateCourseMultipartRequest {

    @Schema(description = "JSON payload for the course metadata")
    private CourseRequest request;

    @Schema(type = "string", format = "binary", description = "Course icon image file")
    private String iconFile;
}
