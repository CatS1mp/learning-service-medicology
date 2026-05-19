package com.medicology.learning.service;

import com.medicology.learning.dto.response.ContentBlockTemplateResponse;
import com.medicology.learning.entity.ContentBlockTemplate;
import com.medicology.learning.repository.ContentBlockTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentBlockTemplateService {
    private final ContentBlockTemplateRepository contentBlockTemplateRepository;

    public List<ContentBlockTemplateResponse> getActiveTemplates() {
        return contentBlockTemplateRepository.findByIsActiveTrueOrderByKindAscNameAsc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ContentBlockTemplateResponse getTemplateDetail(UUID templateId) {
        ContentBlockTemplate template = contentBlockTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu khối nội dung với ID: " + templateId));
        return mapToResponse(template);
    }

    private ContentBlockTemplateResponse mapToResponse(ContentBlockTemplate template) {
        return ContentBlockTemplateResponse.builder()
                .id(template.getId())
                .kind(template.getKind())
                .name(template.getName())
                .description(template.getDescription())
                .payloadSchemaJson(template.getPayloadSchemaJson())
                .starterPayloadJson(template.getStarterPayloadJson())
                .defaultIsGradable(template.getDefaultIsGradable())
                .defaultMaxScore(template.getDefaultMaxScore())
                .allowScoreEdit(template.getAllowScoreEdit())
                .isActive(template.getIsActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
