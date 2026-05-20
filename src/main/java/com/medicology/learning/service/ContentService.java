package com.medicology.learning.service;

import com.medicology.learning.dto.request.ContentBlockProgressUpsertRequest;
import com.medicology.learning.dto.request.ContentRequest;
import com.medicology.learning.dto.request.ContentBlockRequest;
import com.medicology.learning.dto.request.ContentStatusRequest;
import com.medicology.learning.dto.response.ContentBlockInternalResponse;
import com.medicology.learning.dto.response.ContentMetaInternalResponse;
import com.medicology.learning.dto.response.ContentBlockProgressResponse;
import com.medicology.learning.dto.response.ContentBlockResponse;
import com.medicology.learning.dto.response.ContentResponse;
import com.medicology.learning.dto.response.ContentSummaryResponse;
import com.medicology.learning.entity.Content;
import com.medicology.learning.entity.ContentBlock;
import com.medicology.learning.entity.ContentBlockProgress;
import com.medicology.learning.entity.Section;
import com.medicology.learning.exception.InvalidRequestException;
import com.medicology.learning.entity.UserCourseStatus;
import com.medicology.learning.repository.ContentBlockProgressRepository;
import com.medicology.learning.repository.ContentBlockRepository;
import com.medicology.learning.repository.ContentRepository;
import com.medicology.learning.repository.SectionRepository;
import com.medicology.learning.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final SectionRepository sectionRepository;
    private final ContentRepository contentRepository;
    private final ContentBlockRepository contentBlockRepository;
    private final ContentBlockProgressRepository contentBlockProgressRepository;
    private final ContentBlockValidator contentBlockValidator;
    private final UserCourseRepository userCourseRepository;

    public List<ContentSummaryResponse> getContentsBySection(UUID sectionId) {
        return contentRepository.findBySectionIdOrderByOrderIndexAsc(sectionId).stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    public ContentResponse getContentDetail(UUID contentId) {
        return contentRepository.findById(contentId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nội dung với ID: " + contentId));
    }

    @Transactional
    public ContentResponse createContent(ContentRequest request) {
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương với ID: " + request.getSectionId()));
        contentBlockValidator.validate(request.getBlocks());
        Content content = Content.builder()
                .section(section)
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .orderIndex(request.getOrderIndex())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .difficultyLevel(request.getDifficultyLevel())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .content(Objects.requireNonNullElse(request.getContent(), ""))
                .build();
        content.setBlocks(mapBlockRequestsToEntities(content, request.getBlocks()));
        return mapToResponse(contentRepository.save(content));
    }

    @Transactional
    public ContentResponse updateContent(UUID contentId, ContentRequest request) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nội dung với ID: " + contentId));
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương với ID: " + request.getSectionId()));
        contentBlockValidator.validate(request.getBlocks());
        content.setSection(section);
        content.setName(request.getName());
        content.setDescription(request.getDescription());
        content.setSlug(request.getSlug());
        content.setOrderIndex(request.getOrderIndex());
        content.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        content.setDifficultyLevel(request.getDifficultyLevel());
        content.setIsActive(request.getIsActive());
        if (request.getContent() != null) {
            content.setContent(request.getContent());
        }
        if (request.getBlocks() != null) {
            replaceContentBlocks(content, mapBlockRequestsToEntities(content, request.getBlocks()));
        }
        return mapToResponse(contentRepository.save(content));
    }

    /**
     * Must mutate the persistent collection in place — replacing {@code setBlocks(newList)} breaks
     * Hibernate orphanRemoval on {@link Content#getBlocks()}.
     */
    private void replaceContentBlocks(Content content, List<ContentBlock> newBlocks) {
        List<ContentBlock> blocks = content.getBlocks();
        if (blocks == null) {
            content.setBlocks(new ArrayList<>(newBlocks));
            return;
        }
        blocks.clear();
        // Orphan removals are pending until flush; without this Hibernate can INSERT new rows
        // before DELETE runs, violating uk_content_block_content_order (content_id + order_index).
        contentRepository.flush();
        blocks.addAll(newBlocks);
    }

    public ContentResponse updateContentStatus(UUID contentId, ContentStatusRequest request) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nội dung với ID: " + contentId));
        content.setIsActive(request.getIsActive());
        return mapToResponse(contentRepository.save(content));
    }

    public void deleteContent(UUID contentId) {
        if (!contentRepository.existsById(contentId)) {
            throw new IllegalArgumentException("Không tìm thấy nội dung với ID: " + contentId);
        }
        contentRepository.deleteById(contentId);
    }

    @Transactional
    public ContentBlockProgressResponse upsertContentBlockProgress(ContentBlockProgressUpsertRequest request) {
        ContentBlock block = contentBlockRepository.findById(request.contentBlockId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khối nội dung: " + request.contentBlockId()));

        ContentBlockProgress progress = contentBlockProgressRepository
                .findByAttemptIdAndContentBlockId(request.attemptId(), request.contentBlockId())
                .orElseGet(() -> ContentBlockProgress.builder()
                        .attemptId(request.attemptId())
                        .contentBlock(block)
                        .build());

        progress.setAnswerId(request.answerId());
        progress.setContentBlock(block);
        progress.setAttemptId(request.attemptId());
        ContentBlockProgress saved = contentBlockProgressRepository.save(progress);
        return ContentBlockProgressResponse.builder()
                .id(saved.getId())
                .attemptId(saved.getAttemptId())
                .answerId(saved.getAnswerId())
                .contentBlockId(saved.getContentBlock().getId())
                .build();
    }

    public ContentMetaInternalResponse getContentMetaForInternal(UUID contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nội dung: " + contentId));
        return ContentMetaInternalResponse.builder()
                .id(content.getId())
                .estimatedDurationMinutes(content.getEstimatedDurationMinutes())
                .build();
    }

    public ContentBlockInternalResponse getContentBlockForInternal(UUID blockId) {
        ContentBlock block = contentBlockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khối nội dung: " + blockId));
        return ContentBlockInternalResponse.builder()
                .id(block.getId())
                .contentId(block.getContent().getId())
                .kind(block.getKind())
                .payload(block.getPayload())
                .maxScore(block.getMaxScore())
                .isGradable(block.getIsGradable())
                .orderIndex(block.getOrderIndex())
                .build();
    }

    public boolean userCanAccessContent(UUID userId, UUID contentId) {
        Content content = contentRepository.findById(contentId).orElse(null);
        if (content == null) {
            return false;
        }
        UUID courseId = content.getSection().getCourse().getId();
        return userCourseRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, UserCourseStatus.ENROLLED);
    }

    public ContentResponse mapToResponse(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .sectionId(content.getSection().getId())
                .name(content.getName())
                .description(content.getDescription())
                .slug(content.getSlug())
                .orderIndex(content.getOrderIndex())
                .estimatedDurationMinutes(content.getEstimatedDurationMinutes())
                .difficultyLevel(content.getDifficultyLevel())
                .isActive(content.getIsActive())
                .content(content.getContent())
                .blocks(mapToBlockResponses(content.getBlocks()))
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }

    public ContentSummaryResponse mapToSummaryResponse(Content content) {
        return ContentSummaryResponse.builder()
                .id(content.getId())
                .name(content.getName())
                .description(content.getDescription())
                .slug(content.getSlug())
                .orderIndex(content.getOrderIndex())
                .estimatedDurationMinutes(content.getEstimatedDurationMinutes())
                .difficultyLevel(content.getDifficultyLevel())
                .isActive(content.getIsActive())
                .content(content.getContent())
                .blocks(mapToBlockResponses(content.getBlocks()))
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }

    private List<ContentBlock> mapBlockRequestsToEntities(Content content, List<ContentBlockRequest> blocks) {
        if (blocks == null) {
            return null;
        }
        List<ContentBlock> mapped = new ArrayList<>();
        for (ContentBlockRequest request : blocks) {
            boolean shouldGrade = Boolean.TRUE.equals(request.getIsGradable())
                    || (request.getIsGradable() == null && request.getMaxScore() != null);
            Boolean isGradable = shouldGrade;
            Integer maxScore = request.getMaxScore() != null ? request.getMaxScore() : (shouldGrade ? 1 : null);
            if (!Boolean.TRUE.equals(isGradable)) {
                maxScore = null;
            }
            mapped.add(ContentBlock.builder()
                    .content(content)
                    .orderIndex(request.getOrderIndex())
                    .kind(request.getKind())
                    .payload(request.getPayload())
                    .maxScore(maxScore)
                    .isGradable(isGradable)
                    .build());
        }
        return mapped;
    }

    private List<ContentBlockResponse> mapToBlockResponses(List<ContentBlock> blocks) {
        if (blocks == null) {
            return null;
        }
        return blocks.stream()
                .map(block -> ContentBlockResponse.builder()
                        .id(block.getId())
                        .orderIndex(block.getOrderIndex())
                        .kind(block.getKind())
                        .payload(block.getPayload())
                        .maxScore(block.getMaxScore())
                        .isGradable(block.getIsGradable())
                        .createdAt(block.getCreatedAt())
                        .updatedAt(block.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
