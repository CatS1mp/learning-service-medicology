package com.medicology.learning.service;

import com.medicology.learning.dto.request.LessonRequest;
import com.medicology.learning.dto.request.LessonBlockProgressRequest;
import com.medicology.learning.dto.request.LessonContentBlockRequest;
import com.medicology.learning.dto.response.LessonBlockProgressResponse;
import com.medicology.learning.dto.response.LessonContentBlockResponse;
import com.medicology.learning.dto.request.LessonStatusRequest;
import com.medicology.learning.dto.response.LessonResponse;
import com.medicology.learning.dto.response.LessonSummaryResponse;
import com.medicology.learning.entity.BlockProgressStatus;
import com.medicology.learning.entity.Lesson;
import com.medicology.learning.entity.LessonContentBlock;
import com.medicology.learning.entity.Section;
import com.medicology.learning.entity.UserLesson;
import com.medicology.learning.entity.UserLessonBlockProgress;
import com.medicology.learning.exception.InvalidRequestException;
import com.medicology.learning.repository.LessonContentBlockRepository;
import com.medicology.learning.repository.LessonRepository;
import com.medicology.learning.repository.SectionRepository;
import com.medicology.learning.repository.UserLessonBlockProgressRepository;
import com.medicology.learning.repository.UserLessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final UserLessonRepository userLessonRepository;
    private final LessonContentBlockRepository lessonContentBlockRepository;
    private final UserLessonBlockProgressRepository userLessonBlockProgressRepository;
    private final LessonContentBlockValidator lessonContentBlockValidator;

    public List<LessonSummaryResponse> getLessonsBySection(UUID sectionId) {
        return lessonRepository.findBySectionIdOrderByOrderIndexAsc(sectionId).stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse getLessonDetail(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + lessonId));
    }

    @Transactional
    public LessonResponse enrollLesson(UUID lessonId, UUID userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + lessonId));
        userLessonRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> userLessonRepository.save(UserLesson.builder()
                        .userId(userId)
                        .lessonId(lessonId)
                        .lesson(lesson)
                        .quizzesCorrect(0)
                        .build()));
        return mapToResponse(lesson);
    }

    public void completeLesson(UUID lessonId, UUID userId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + lessonId));

        UserLesson userLesson = userLessonRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> UserLesson.builder()
                        .userId(userId)
                        .lessonId(lessonId)
                        .lesson(lesson)
                        .quizzesCorrect(0)
                        .build());

        userLesson.setLesson(lesson);
        userLesson.setCompletedAt(LocalDateTime.now());
        if (userLesson.getQuizzesCorrect() == null) {
            userLesson.setQuizzesCorrect(0);
        }
        userLessonRepository.save(userLesson);
    }

    public LessonResponse createLesson(LessonRequest request) {
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + request.getSectionId()));
        lessonContentBlockValidator.validate(request.getBlocks());
        Lesson lesson = Lesson.builder()
                .section(section)
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .orderIndex(request.getOrderIndex())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .difficultyLevel(request.getDifficultyLevel())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .content(request.getContent())
                .build();
        lesson.setBlocks(mapBlockRequestsToEntities(lesson, request.getBlocks()));
        return mapToResponse(lessonRepository.save(lesson));
    }

    public LessonResponse updateLesson(UUID lessonId, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + lessonId));
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + request.getSectionId()));
        lessonContentBlockValidator.validate(request.getBlocks());
        lesson.setSection(section);
        lesson.setName(request.getName());
        lesson.setDescription(request.getDescription());
        lesson.setSlug(request.getSlug());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        lesson.setDifficultyLevel(request.getDifficultyLevel());
        lesson.setIsActive(request.getIsActive());
        lesson.setContent(request.getContent());
        if (request.getBlocks() != null) {
            lesson.setBlocks(mapBlockRequestsToEntities(lesson, request.getBlocks()));
        }
        return mapToResponse(lessonRepository.save(lesson));
    }

    public LessonBlockProgressResponse updateBlockProgress(UUID lessonId, UUID blockId, UUID userId,
                                                           LessonBlockProgressRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + lessonId));

        LessonContentBlock block = lessonContentBlockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson block not found with ID: " + blockId));

        if (!block.getLesson().getId().equals(lesson.getId())) {
            throw new InvalidRequestException("Block does not belong to lesson: " + lessonId);
        }

        UserLessonBlockProgress progress = userLessonBlockProgressRepository
                .findByUserIdAndLessonContentBlockId(userId, blockId)
                .orElseGet(() -> UserLessonBlockProgress.builder()
                        .userId(userId)
                        .lessonId(lessonId)
                        .lessonContentBlock(block)
                        .status(BlockProgressStatus.NOT_STARTED)
                        .build());

        BlockProgressStatus status = request.getStatus() == null ? BlockProgressStatus.IN_PROGRESS : request.getStatus();
        progress.setStatus(status);
        progress.setScore(request.getScore());
        progress.setMaxScore(request.getMaxScore());
        progress.setLessonId(lessonId);
        progress.setLessonContentBlock(block);
        progress.setCompletedAt(status == BlockProgressStatus.COMPLETED ? LocalDateTime.now() : null);

        return mapToBlockProgressResponse(userLessonBlockProgressRepository.save(progress));
    }

    public List<LessonBlockProgressResponse> getBlockProgress(UUID lessonId, UUID userId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Lesson not found with ID: " + lessonId);
        }

        return userLessonBlockProgressRepository.findByUserIdAndLessonId(userId, lessonId).stream()
                .map(this::mapToBlockProgressResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse updateLessonStatus(UUID lessonId, LessonStatusRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + lessonId));
        lesson.setIsActive(request.getIsActive());
        return mapToResponse(lessonRepository.save(lesson));
    }

    public void deleteLesson(UUID lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Lesson not found with ID: " + lessonId);
        }
        lessonRepository.deleteById(lessonId);
    }

    public LessonResponse mapToResponse(Lesson lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .sectionId(lesson.getSection().getId())
                .name(lesson.getName())
                .description(lesson.getDescription())
                .slug(lesson.getSlug())
                .orderIndex(lesson.getOrderIndex())
                .estimatedDurationMinutes(lesson.getEstimatedDurationMinutes())
                .difficultyLevel(lesson.getDifficultyLevel())
                .isActive(lesson.getIsActive())
                .content(lesson.getContent())
                .blocks(mapToBlockResponses(lesson.getBlocks()))
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    public LessonSummaryResponse mapToSummaryResponse(Lesson lesson) {
        return LessonSummaryResponse.builder()
                .id(lesson.getId())
                .name(lesson.getName())
                .description(lesson.getDescription())
                .slug(lesson.getSlug())
                .orderIndex(lesson.getOrderIndex())
                .estimatedDurationMinutes(lesson.getEstimatedDurationMinutes())
                .difficultyLevel(lesson.getDifficultyLevel())
                .isActive(lesson.getIsActive())
                .content(lesson.getContent())
                .blocks(mapToBlockResponses(lesson.getBlocks()))
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    private List<LessonContentBlock> mapBlockRequestsToEntities(Lesson lesson, List<LessonContentBlockRequest> blocks) {
        if (blocks == null) {
            return null;
        }

        List<LessonContentBlock> mapped = new ArrayList<>();
        for (LessonContentBlockRequest request : blocks) {
            mapped.add(LessonContentBlock.builder()
                    .lesson(lesson)
                    .orderIndex(request.getOrderIndex())
                    .kind(request.getKind())
                    .payload(request.getPayload())
                    .assessmentId(request.getAssessmentId())
                    .questionId(request.getQuestionId())
                    .build());
        }
        return mapped;
    }

    private List<LessonContentBlockResponse> mapToBlockResponses(List<LessonContentBlock> blocks) {
        if (blocks == null) {
            return null;
        }

        return blocks.stream()
                .map(block -> LessonContentBlockResponse.builder()
                        .id(block.getId())
                        .orderIndex(block.getOrderIndex())
                        .kind(block.getKind())
                        .payload(block.getPayload())
                        .assessmentId(block.getAssessmentId())
                        .questionId(block.getQuestionId())
                        .createdAt(block.getCreatedAt())
                        .updatedAt(block.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private LessonBlockProgressResponse mapToBlockProgressResponse(UserLessonBlockProgress progress) {
        return LessonBlockProgressResponse.builder()
                .blockId(progress.getLessonContentBlock().getId())
                .status(progress.getStatus())
                .score(progress.getScore())
                .maxScore(progress.getMaxScore())
                .completedAt(progress.getCompletedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}
