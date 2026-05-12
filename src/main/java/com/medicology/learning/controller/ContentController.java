package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.request.ContentRequest;
import com.medicology.learning.dto.request.ContentStatusRequest;
import com.medicology.learning.dto.response.ContentResponse;
import com.medicology.learning.dto.response.ContentSummaryResponse;
import com.medicology.learning.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;

    @GetMapping("/sections/{sectionId}/contents")
    public ResponseEntity<ApiResponse<List<ContentSummaryResponse>>> getContentsBySection(@PathVariable UUID sectionId) {
        return ResponseEntity.ok(ApiResponse.success(contentService.getContentsBySection(sectionId)));
    }

    @GetMapping("/contents/{contentId}")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentDetail(@PathVariable UUID contentId) {
        return ResponseEntity.ok(ApiResponse.success(contentService.getContentDetail(contentId)));
    }

    @PostMapping({"/contents", "/sections/{sectionId}/contents"})
    public ResponseEntity<ApiResponse<ContentResponse>> createContent(@PathVariable(required = false) UUID sectionId,
            @RequestBody ContentRequest request) {
        if (sectionId != null) {
            request.setSectionId(sectionId);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Content created successfully",
                        contentService.createContent(request)));
    }

    @PutMapping("/contents/{contentId}")
    public ResponseEntity<ApiResponse<ContentResponse>> updateContent(@PathVariable UUID contentId, @RequestBody ContentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Content updated successfully",
                contentService.updateContent(contentId, request)));
    }

    @DeleteMapping("/contents/{contentId}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(@PathVariable UUID contentId) {
        contentService.deleteContent(contentId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Content deleted successfully", null));
    }

    @PatchMapping("/contents/{contentId}/status")
    public ResponseEntity<ApiResponse<ContentResponse>> updateContentStatus(@PathVariable UUID contentId, @RequestBody ContentStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Content status updated successfully",
                contentService.updateContentStatus(contentId, request)));
    }
}
