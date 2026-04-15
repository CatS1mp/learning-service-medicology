package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.response.CourseProgressResponse;
import com.medicology.learning.dto.response.LessonActivitySummaryResponse;
import com.medicology.learning.entity.UserDailyStreak;
import com.medicology.learning.service.ProgressService;
import com.medicology.learning.wrapper.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;

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
    public ResponseEntity<ApiResponse<List<CourseProgressResponse>>> getProgressByUserId(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (!user.getId().equals(userId) && !user.isAdmin()) {
            throw new ResponseStatusException(FORBIDDEN, "Cannot view another user's progress");
        }
        return ResponseEntity.ok(ApiResponse.success(progressService.getUserProgress(userId)));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<LessonActivitySummaryResponse>> getLessonActivity(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.success(progressService.getLessonActivity(user.getId(), days)));
    }

    @PostMapping("/streak/ping")
    public ResponseEntity<ApiResponse<UserDailyStreak>> pingStreak(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Streak updated successfully",
                progressService.updateStreak(user.getId())));
    }

    @GetMapping("/streak/ping")
    public ResponseEntity<ApiResponse<UserDailyStreak>> pingStreakGet(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Streak updated successfully",
                progressService.updateStreak(user.getId())));
    }
}
