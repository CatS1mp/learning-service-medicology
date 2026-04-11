package com.medicology.learning.dto.request;

import com.medicology.learning.entity.BlockProgressStatus;
import lombok.Data;

@Data
public class LessonBlockProgressRequest {
    private BlockProgressStatus status;
    private Integer score;
    private Integer maxScore;
}
