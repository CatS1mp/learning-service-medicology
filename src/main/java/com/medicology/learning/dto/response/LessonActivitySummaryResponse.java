package com.medicology.learning.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LessonActivitySummaryResponse {
    private Integer totalCompletedLessons;
    private List<LessonActivityResponse> activities;
}
