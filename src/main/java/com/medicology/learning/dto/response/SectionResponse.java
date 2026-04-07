package com.medicology.learning.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SectionResponse {
    private UUID id;
    private UUID courseId;
    private String name;
    private String slug;
    private Integer orderIndex;
    private Integer estimatedDurationMinutes;
    private List<LessonSummaryResponse> lessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
