package com.medicology.learning.dto.request;

import lombok.Data;

@Data
public class CourseRequest {
    private String name;
    private String slug;
    private String description;
    private String iconFileName;
    private String colorCode;
    private Integer orderIndex;
}
