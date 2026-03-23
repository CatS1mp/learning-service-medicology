package com.medicology.learning.controller;

import com.medicology.learning.entity.SectionTest;
import com.medicology.learning.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/learning/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    private UUID getUserId(String email) {
        return UUID.nameUUIDFromBytes(email.getBytes());
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<SectionTest> getSectionTest(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(testService.getSectionTestContent(sectionId));
    }

    @PostMapping("/course/{courseId}/submit")
    public ResponseEntity<String> submitCourseQuiz(
            @PathVariable UUID courseId, 
            @AuthenticationPrincipal String email,
            @RequestBody Map<String, Integer> request) {
        testService.submitCourseQuiz(getUserId(email), courseId, request.getOrDefault("quizzesCorrect", 0));
        return ResponseEntity.ok("Course quiz submitted");
    }

    @PostMapping("/section/{sectionId}/submit")
    public ResponseEntity<String> submitSectionTest(
            @PathVariable UUID sectionId, 
            @AuthenticationPrincipal String email,
            @RequestBody Map<String, Integer> request) {
        testService.submitSectionTest(
            getUserId(email), 
            sectionId, 
            request.getOrDefault("quizzesCorrect", 0),
            request.getOrDefault("totalQuestions", 10)
        );
        return ResponseEntity.ok("Section test submitted");
    }

    @GetMapping("/results")
    public ResponseEntity<?> getResults(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(testService.getUserTestResults(getUserId(email)));
    }
}
