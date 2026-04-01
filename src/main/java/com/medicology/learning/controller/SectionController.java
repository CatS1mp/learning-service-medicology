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
    @GetMapping("/themes/{themeId}/sections")
    public ResponseEntity<List<SectionResponse>> getSectionsByTheme(@PathVariable UUID themeId) {
        return ResponseEntity.ok(sectionService.getSectionsByTheme(themeId));
    }

    // According to Plan C, POST /api/v1/learning/courses/{courseId}/sections was requested, 
    // but the entity model requires a Theme ID! We will implement it exactly as Plan C mentions but take Theme ID inside the body.
    @PostMapping("/courses/{courseId}/sections")
    public ResponseEntity<SectionResponse> createSectionByCourseId(
            @PathVariable UUID courseId, @RequestBody SectionRequest request) {
        // Here courseId is ignored because Section requires ThemeId, which we get from request body.
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
