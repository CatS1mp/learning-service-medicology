package com.medicology.learning.dto.response;

import com.medicology.learning.entity.BlockProgressStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LessonBlockProgressResponse {
    private UUID blockId;
    private BlockProgressStatus status;
    private Integer score;
    private Integer maxScore;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
