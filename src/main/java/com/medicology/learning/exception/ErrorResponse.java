package com.medicology.learning.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp
) {}
