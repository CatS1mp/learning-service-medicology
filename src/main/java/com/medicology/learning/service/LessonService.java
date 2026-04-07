package com.medicology.learning.service;

import com.medicology.learning.dto.request.LessonRequest;
import com.medicology.learning.dto.request.LessonStatusRequest;
import com.medicology.learning.dto.response.LessonResponse;
import com.medicology.learning.dto.response.LessonSummaryResponse;
import com.medicology.learning.entity.Lesson;
import com.medicology.learning.entity.Section;
import com.medicology.learning.repository.LessonRepository;
import com.medicology.learning.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;

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

    public LessonResponse createLesson(LessonRequest request) {
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + request.getSectionId()));
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
        return mapToResponse(lessonRepository.save(lesson));
    }

    public LessonResponse updateLesson(UUID lessonId, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + lessonId));
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + request.getSectionId()));
        lesson.setSection(section);
        lesson.setName(request.getName());
        lesson.setDescription(request.getDescription());
        lesson.setSlug(request.getSlug());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        lesson.setDifficultyLevel(request.getDifficultyLevel());
        lesson.setIsActive(request.getIsActive());
        lesson.setContent(request.getContent());
        return mapToResponse(lessonRepository.save(lesson));
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
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
