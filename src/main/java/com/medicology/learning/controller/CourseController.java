package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.request.CourseRequest;
import com.medicology.learning.dto.response.CourseResponse;
import com.medicology.learning.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        return ResponseEntity.ok(ApiResponse.success(courseService.getAllCourses()));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseDetail(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseById(courseId)));
    }

    @GetMapping("/{courseId}/roadmap")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseRoadmap(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseRoadmap(courseId)));
    }

    @GetMapping("/path")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLearningPath() {
        return ResponseEntity.ok(ApiResponse.success(courseService.getLearningPath()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Course created successfully",
                        courseService.createCourse(request)));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable UUID courseId, @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Course updated successfully",
                courseService.updateCourse(courseId, request)));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Course deleted successfully", null));
    }
}
