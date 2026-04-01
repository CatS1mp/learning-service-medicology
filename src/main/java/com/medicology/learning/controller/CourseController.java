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
@RequestMapping("/api/v1/learning/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllThemes());
    }

    @GetMapping("/path")
    public ResponseEntity<?> getLearningPath() {
        return ResponseEntity.ok(courseService.getLearningPath());
    }

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<String> enrollCourse(@PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal user) {
        courseService.enrollCourse(user.getId(), courseId);
        return ResponseEntity.ok("Enrolled successfully");
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseDetail(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getCourseContent(courseId));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable UUID courseId, @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, request));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{courseId}/status")
    public ResponseEntity<CourseResponse> updateCourseStatus(@PathVariable UUID courseId, @RequestBody CourseStatusRequest request) {
        return ResponseEntity.ok(courseService.updateCourseStatus(courseId, request));
    }
}
