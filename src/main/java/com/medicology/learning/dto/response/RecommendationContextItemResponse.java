package com.medicology.learning.dto.response;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationContextItemResponse {
    private UUID contentId;
    private String contentName;
    private String courseName;
    private String sectionName;
    private Instant submittedAt;
    private Boolean passed;
}
