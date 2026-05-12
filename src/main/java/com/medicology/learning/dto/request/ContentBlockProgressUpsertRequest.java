package com.medicology.learning.dto.request;

import java.util.UUID;

public record ContentBlockProgressUpsertRequest(UUID attemptId, UUID answerId, UUID contentBlockId) {}
