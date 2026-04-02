package com.medicology.learning.controller;

import com.medicology.learning.dto.request.CourseRequest;
import com.medicology.learning.dto.request.CourseStatusRequest;
import com.medicology.learning.dto.response.CourseResponse;
import com.medicology.learning.entity.Course;
import com.medicology.learning.service.CourseService;
import com.medicology.learning.wrapper.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/lessons")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @PostMapping("/{lessonId}/enroll")
    public ResponseEntity<Course> enrollCourse(@PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal user) {
        // Just return the content of the lesson for now, skipping the old empty row creation
        return ResponseEntity.ok(courseService.getCourseContent(lessonId));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<Course> getCourseDetail(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(courseService.getCourseContent(lessonId));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable UUID lessonId, @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(lessonId, request));
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID lessonId) {
        courseService.deleteCourse(lessonId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{lessonId}/status")
    public ResponseEntity<CourseResponse> updateCourseStatus(@PathVariable UUID lessonId, @RequestBody CourseStatusRequest request) {
        return ResponseEntity.ok(courseService.updateCourseStatus(lessonId, request));
    }
}
