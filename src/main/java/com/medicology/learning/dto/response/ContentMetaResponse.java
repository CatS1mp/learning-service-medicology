package com.medicology.learning.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentMetaResponse {
    private UUID contentId;
    private String contentName;
    private String courseName;
    private String sectionName;
}
