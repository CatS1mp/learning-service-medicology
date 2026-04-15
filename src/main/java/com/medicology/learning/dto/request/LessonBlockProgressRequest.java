package com.medicology.learning.dto.request;

import com.medicology.learning.entity.BlockProgressStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class LessonBlockProgressRequest {
    private BlockProgressStatus status;
    private UUID attemptId;
}
