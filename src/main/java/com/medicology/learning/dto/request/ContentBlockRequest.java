package com.medicology.learning.dto.request;

import com.medicology.learning.entity.ContentBlockKind;
import lombok.Data;

@Data
public class ContentBlockRequest {
    private Integer orderIndex;
    private ContentBlockKind kind;
    private String payload;
    private Integer maxScore;
    private Boolean isGradable;
}
