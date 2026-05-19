package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.entity.UserDailyStreak;
import com.medicology.learning.service.ProgressService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning/internal")
@RequiredArgsConstructor
public class LearningInternalController {

    private final ProgressService progressService;

    @PostMapping("/users/{userId}/streak/ping")
    public ResponseEntity<ApiResponse<UserDailyStreak>> pingStreak(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(progressService.updateStreak(userId)));
    }
}
