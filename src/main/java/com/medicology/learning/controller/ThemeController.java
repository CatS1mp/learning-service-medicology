package com.medicology.learning.controller;

import com.medicology.learning.dto.request.ThemeRequest;
import com.medicology.learning.dto.response.ThemeResponse;
import com.medicology.learning.service.CourseService;
import com.medicology.learning.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/courses")
@RequiredArgsConstructor
public class ThemeController {
    private final ThemeService themeService;
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllCourses() {
        return ResponseEntity.ok(themeService.getAllThemes());
    }

    @GetMapping("/path")
    public ResponseEntity<?> getLearningPath() {
        return ResponseEntity.ok(courseService.getLearningPath());
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createCourse(@RequestBody ThemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(themeService.createTheme(request));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ThemeResponse> updateCourse(@PathVariable UUID courseId, @RequestBody ThemeRequest request) {
        return ResponseEntity.ok(themeService.updateTheme(courseId, request));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        themeService.deleteTheme(courseId);
        return ResponseEntity.noContent().build();
    }
}
