package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.response.ContentBlockTemplateResponse;
import com.medicology.learning.service.ContentBlockTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class ContentBlockTemplateController {
    private final ContentBlockTemplateService contentBlockTemplateService;

    @GetMapping("/block-templates")
    public ResponseEntity<ApiResponse<List<ContentBlockTemplateResponse>>> getActiveTemplates() {
        return ResponseEntity.ok(ApiResponse.success(contentBlockTemplateService.getActiveTemplates()));
    }

    @GetMapping("/block-templates/{templateId}")
    public ResponseEntity<ApiResponse<ContentBlockTemplateResponse>> getTemplateDetail(@PathVariable UUID templateId) {
        return ResponseEntity.ok(ApiResponse.success(contentBlockTemplateService.getTemplateDetail(templateId)));
    }
}
