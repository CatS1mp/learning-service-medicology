package com.medicology.learning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.request.CreateCourseMultipartRequest;
import com.medicology.learning.dto.request.CourseRequest;
import com.medicology.learning.dto.response.CourseResponse;
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
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        return ResponseEntity.ok(ApiResponse.success(courseService.getAllCourses()));
    }

    @GetMapping("/enrolled")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getEnrolledCourses(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getEnrolledCourses(user.getId())));
    }

    @GetMapping("/student/available")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAvailableCoursesForStudent(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(courseService.getAvailableCoursesForStudent(user.getId())));
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Parameter(hidden = true) @RequestPart("request") String requestJson,
            @Parameter(hidden = true) @RequestPart("iconFile") MultipartFile iconFile) {
        CourseRequest request = parseCourseRequest(requestJson);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Course created successfully",
                        courseService.createCourse(request, iconFile)));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable UUID courseId, @Valid @RequestBody CourseRequest request) {
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
            throw new InvalidRequestException("Part 'request' must contain valid JSON for course data", ex);
        }
    }
}
