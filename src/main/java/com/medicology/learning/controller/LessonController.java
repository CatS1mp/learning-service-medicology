package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.request.LessonBlockProgressRequest;
import com.medicology.learning.dto.request.LessonRequest;
import com.medicology.learning.dto.request.LessonStatusRequest;
import com.medicology.learning.dto.response.LessonBlockProgressResponse;
import com.medicology.learning.dto.response.LessonResponse;
import com.medicology.learning.dto.response.LessonSummaryResponse;
import com.medicology.learning.service.LessonService;
import com.medicology.learning.wrapper.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;

    @GetMapping("/sections/{sectionId}/lessons")
    public ResponseEntity<ApiResponse<List<LessonSummaryResponse>>> getLessonsBySection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(ApiResponse.success(lessonService.getLessonsBySection(sectionId)));
    }

    @PostMapping("/lessons/{lessonId}/enroll")
    public ResponseEntity<ApiResponse<LessonResponse>> enrollLesson(@PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Lesson enrollment fetched successfully",
                lessonService.getLessonDetail(lessonId)));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeLesson(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal user) {
        lessonService.completeLesson(lessonId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lesson completed successfully", null));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse>> getLessonDetail(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(ApiResponse.success(lessonService.getLessonDetail(lessonId)));
    }

    @PostMapping({"/lessons", "/sections/{sectionId}/lessons"})
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(@PathVariable(required = false) UUID sectionId,
            @RequestBody LessonRequest request) {
        if (sectionId != null) {
            request.setSectionId(sectionId);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Lesson created successfully",
                        lessonService.createLesson(request)));
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(@PathVariable UUID lessonId, @RequestBody LessonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Lesson updated successfully",
                lessonService.updateLesson(lessonId, request)));
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable UUID lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lesson deleted successfully", null));
    }

    @PatchMapping("/lessons/{lessonId}/status")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLessonStatus(@PathVariable UUID lessonId, @RequestBody LessonStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Lesson status updated successfully",
                lessonService.updateLessonStatus(lessonId, request)));
    }

    @PatchMapping("/lessons/{lessonId}/blocks/{blockId}/progress")
    public ResponseEntity<ApiResponse<LessonBlockProgressResponse>> updateBlockProgress(
            @PathVariable UUID lessonId,
            @PathVariable UUID blockId,
            @RequestBody LessonBlockProgressRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Lesson block progress updated successfully",
                lessonService.updateBlockProgress(lessonId, blockId, user.getId(), request)));
    }

    @GetMapping("/lessons/{lessonId}/blocks/progress")
    public ResponseEntity<ApiResponse<List<LessonBlockProgressResponse>>> getBlockProgress(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Lesson block progress fetched successfully",
                lessonService.getBlockProgress(lessonId, user.getId())));
    }
}
