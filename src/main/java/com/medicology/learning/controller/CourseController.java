package com.medicology.learning.controller;

import com.medicology.learning.entity.Course;
import com.medicology.learning.service.CourseService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<String> enrollCourse(@PathVariable UUID courseId, @AuthenticationPrincipal String email) {
        // Lưu ý: Tạm thời dùng Fake UUID cho userId vì JWT hiện tại trả về Chủ thể là Email.
        // Bạn cần update Auth Service để đưa UUID vào Token hoặc query user_id từ database tại đây.
        UUID fakeUserId = UUID.nameUUIDFromBytes(email.getBytes());
        courseService.enrollCourse(fakeUserId, courseId);
        return ResponseEntity.ok("Enrolled successfully");
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseDetail(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getCourseContent(courseId));
    }
}
