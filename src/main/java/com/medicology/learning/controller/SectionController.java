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

    @GetMapping("/courses/{courseId}/sections")
    public ResponseEntity<List<SectionResponse>> getSectionsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(sectionService.getSectionsByCourse(courseId));
    }

    @PostMapping("/courses/{courseId}/sections")
    public ResponseEntity<SectionResponse> createSectionInCourse(
            @PathVariable UUID courseId, @RequestBody SectionRequest request) {
        request.setCourseId(courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.createSection(request));
    }

    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<SectionResponse> getSectionDetail(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(sectionService.getSectionById(sectionId));
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
