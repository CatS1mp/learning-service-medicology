package com.medicology.learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.learning.common.pagination.PaginatedResponse;
import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.request.CreateCourseMultipartRequest;
import com.medicology.learning.dto.request.CourseRequest;
import com.medicology.learning.dto.response.CourseResponse;
import com.medicology.learning.dto.response.CourseRoadmapResponse;
import com.medicology.learning.service.CourseRoadmapService;
import com.medicology.learning.service.ProgressService;
import com.medicology.learning.exception.InvalidRequestException;
import com.medicology.learning.service.CourseService;
import com.medicology.learning.wrapper.UserPrincipal;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseRoadmapService courseRoadmapService;
    private final ProgressService progressService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<CourseResponse>>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.fromList(courseService.getAllCourses(), page, size)));
    }

    @GetMapping("/enrolled")
    public ResponseEntity<ApiResponse<PaginatedResponse<CourseResponse>>> getEnrolledCourses(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.fromList(courseService.getEnrolledCourses(user.getId()), page, size)));
    }

    @GetMapping("/student/available")
    public ResponseEntity<ApiResponse<PaginatedResponse<CourseResponse>>> getAvailableCoursesForStudent(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.fromList(courseService.getAvailableCoursesForStudent(user.getId()), page, size)));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseDetail(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseById(courseId)));
    }

    @GetMapping("/{courseId}/roadmap")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseRoadmap(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getCourseRoadmap(courseId)));
    }

    @GetMapping("/slug/{slug}/learner-roadmap")
    public ResponseEntity<ApiResponse<CourseRoadmapResponse>> getLearnerRoadmapBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal UserPrincipal user) {
        var snapshot = progressService.loadSnapshot(user.getId());
        return ResponseEntity.ok(ApiResponse.success(courseRoadmapService.buildLearnerRoadmap(slug, snapshot)));
    }

    @GetMapping("/path")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLearningPath() {
        return ResponseEntity.ok(ApiResponse.success(courseService.getLearningPath()));
    }

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<ApiResponse<Void>> enrollCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal user) {
        courseService.enrollCourse(user.getId(), courseId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Course enrolled successfully",
                        null));
    }

    @Operation(
            summary = "Create a course",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CreateCourseMultipartRequest.class)
                    )
            )
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Parameter(hidden = true) @RequestPart("request") String requestJson,
            @Parameter(hidden = true) @RequestPart("iconFile") MultipartFile iconFile) {
        CourseRequest request = parseCourseRequest(requestJson);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Course created successfully",
                        courseService.createCourse(request, iconFile)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable UUID courseId, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Course updated successfully",
                courseService.updateCourse(courseId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Course deleted successfully", null));
    }

    private CourseRequest parseCourseRequest(String requestJson) {
        try {
            CourseRequest request = objectMapper.readValue(requestJson, CourseRequest.class);
            Set<ConstraintViolation<CourseRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return request;
        } catch (ConstraintViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidRequestException("Phần 'request' phải chứa JSON hợp lệ cho dữ liệu khóa học.", ex);
        }
    }
}
