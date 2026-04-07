package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.request.SectionRequest;
import com.medicology.learning.dto.response.SectionResponse;
import com.medicology.learning.dto.response.SectionSummaryResponse;
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
    public ResponseEntity<ApiResponse<List<SectionSummaryResponse>>> getSectionsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.getSectionsByCourse(courseId)));
    }

    @PostMapping("/courses/{courseId}/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> createSectionInCourse(
            @PathVariable UUID courseId, @RequestBody SectionRequest request) {
        request.setCourseId(courseId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Section created successfully",
                        sectionService.createSection(request)));
    }

    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<ApiResponse<SectionResponse>> getSectionDetail(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(ApiResponse.success(sectionService.getSectionById(sectionId)));
    }

    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(@PathVariable UUID sectionId, @RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Section updated successfully",
                sectionService.updateSection(sectionId, request)));
    }

    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(@PathVariable UUID sectionId) {
        sectionService.deleteSection(sectionId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Section deleted successfully", null));
    }
}
