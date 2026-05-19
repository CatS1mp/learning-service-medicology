package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.response.CourseProgressResponse;
import com.medicology.learning.dto.response.ContentActivitySummaryResponse;
import com.medicology.learning.dto.response.DashboardProgressResponse;
import com.medicology.learning.dto.response.RecommendationContextItemResponse;
import com.medicology.learning.service.RecommendationContextService;
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
    private final RecommendationContextService recommendationContextService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseProgressResponse>>> getProgress(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(progressService.getUserProgress(user.getId())));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<CourseProgressResponse>>> getProgressByUserId(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal user) {
        if (!user.getId().equals(userId) && !user.isAdmin()) {
            throw new ResponseStatusException(FORBIDDEN, "Không thể xem tiến độ của người dùng khác.");
        }
        return ResponseEntity.ok(ApiResponse.success(progressService.getUserProgress(userId)));
    }

    @GetMapping("/recommendation-context")
    public ResponseEntity<ApiResponse<java.util.List<RecommendationContextItemResponse>>> getRecommendationContext(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "8") int limit) {
        var snapshot = progressService.loadSnapshot(user.getId());
        return ResponseEntity.ok(ApiResponse.success(
                recommendationContextService.buildRecentContext(snapshot, limit)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardProgressResponse>> getDashboardProgress(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "7") int activityDays) {
        return ResponseEntity.ok(ApiResponse.success(progressService.getDashboardProgress(user.getId(), activityDays)));
    }

    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<ContentActivitySummaryResponse>> getContentActivity(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(ApiResponse.success(progressService.getContentActivity(user.getId(), days)));
    }

    @PostMapping("/streak/ping")
    public ResponseEntity<ApiResponse<UserDailyStreak>> pingStreak(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Đã cập nhật chuỗi ngày học.",
                progressService.updateStreak(user.getId())));
    }

    @GetMapping("/streak/ping")
    public ResponseEntity<ApiResponse<UserDailyStreak>> pingStreakGet(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Đã cập nhật chuỗi ngày học.",
                progressService.updateStreak(user.getId())));
    }
}
