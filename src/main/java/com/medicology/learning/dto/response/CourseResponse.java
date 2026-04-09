package com.medicology.learning.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CourseResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String iconFileName;
    private String colorCode;
    /** Number of sections (chặng) in the course; set even when {@code sections} list is omitted. */
    private int sectionCount;
    /** Total lessons across all sections; set even when {@code sections} list is omitted. */
    private int lessonCount;
    private List<SectionSummaryResponse> sections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
