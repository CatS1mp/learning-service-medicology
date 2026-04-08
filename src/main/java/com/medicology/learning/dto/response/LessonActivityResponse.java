package com.medicology.learning.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LessonActivityResponse {
    private LocalDate date;
    private Integer completedLessons;
}
