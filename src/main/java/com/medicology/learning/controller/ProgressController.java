package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.response.CourseProgressResponse;
import com.medicology.learning.entity.UserDailyStreak;
import com.medicology.learning.service.ProgressService;
import com.medicology.learning.wrapper.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/progress")
@RequiredArgsConstructor
public class ProgressController {
    private final ProgressService progressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseProgressResponse>>> getProgress(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(progressService.getUserProgress(user.getId())));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<CourseProgressResponse>>> getProgressByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(progressService.getUserProgress(userId)));
    }

    @PostMapping("/streak/ping")
    public ResponseEntity<ApiResponse<UserDailyStreak>> pingStreak(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Streak updated successfully",
                progressService.updateStreak(user.getId())));
    }
}
