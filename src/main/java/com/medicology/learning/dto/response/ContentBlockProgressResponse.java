package com.medicology.learning.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ContentBlockProgressResponse {
    private UUID id;
    private UUID attemptId;
    private UUID answerId;
    private UUID contentBlockId;
}
