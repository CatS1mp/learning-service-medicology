package com.medicology.learning.controller;

import com.medicology.learning.dto.request.SectionRequest;
import com.medicology.learning.dto.response.SectionResponse;
import com.medicology.learning.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class SectionController {
    private final SectionService sectionService;

    // Based on Plan C: GET /api/v1/learning/courses/{courseId}/sections implies getting sections for a course.
    // However, Course -> Section is N -> 1. This means course belongs to a section.
    // It is possible the requirement meant GET /api/v1/learning/themes/{themeId}/sections instead. 
    // We will map it to /themes/{themeId}/sections since that is the correct relational hierarchy.
    // Moved to /courses/{courseId}/sections to align with domain naming
    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<List<SectionResponse>> getSectionsByTheme(@PathVariable UUID courseId) {
        return ResponseEntity.ok(sectionService.getSectionsByTheme(courseId));
    }

    // According to Plan C, POST /api/v1/learning/courses/{courseId}/sections was requested
    @PostMapping("/courses/{courseId}/sections")
    public ResponseEntity<SectionResponse> createSectionByCourseId(
            @PathVariable UUID courseId, @RequestBody SectionRequest request) {
        request.setThemeId(courseId); // Make sure DTO handles themeId mapping correctly
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.createSection(request));
    }

    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<SectionResponse> updateSection(@PathVariable UUID sectionId, @RequestBody SectionRequest request) {
        return ResponseEntity.ok(sectionService.updateSection(sectionId, request));
    }

    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable UUID sectionId) {
        sectionService.deleteSection(sectionId);
        return ResponseEntity.noContent().build();
    }
}
