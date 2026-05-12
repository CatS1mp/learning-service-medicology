package com.medicology.learning.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentActivityResponse {
    private LocalDate date;
    private Integer completedContents;
}
