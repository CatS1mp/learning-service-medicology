package com.medicology.learning.controller;

import com.medicology.learning.dto.request.LessonRequest;
import com.medicology.learning.dto.request.LessonStatusRequest;
import com.medicology.learning.dto.response.LessonResponse;
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
    public ResponseEntity<List<LessonResponse>> getLessonsBySection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(lessonService.getLessonsBySection(sectionId));
    }

    @PostMapping({"/lessons/{lessonId}/enroll", "/courses/{lessonId}/enroll"})
    public ResponseEntity<LessonResponse> enrollLesson(@PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(lessonService.getLessonDetail(lessonId));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonDetail(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(lessonService.getLessonDetail(lessonId));
    }

    @PostMapping({"/lessons", "/sections/{sectionId}/lessons"})
    public ResponseEntity<LessonResponse> createLesson(@PathVariable(required = false) UUID sectionId,
            @RequestBody LessonRequest request) {
        if (sectionId != null) {
            request.setSectionId(sectionId);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(request));
    }

    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable UUID lessonId, @RequestBody LessonRequest request) {
        return ResponseEntity.ok(lessonService.updateLesson(lessonId, request));
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/lessons/{lessonId}/status")
    public ResponseEntity<LessonResponse> updateLessonStatus(@PathVariable UUID lessonId, @RequestBody LessonStatusRequest request) {
        return ResponseEntity.ok(lessonService.updateLessonStatus(lessonId, request));
    }
}
