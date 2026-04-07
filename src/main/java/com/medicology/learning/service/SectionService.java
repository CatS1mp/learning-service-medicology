package com.medicology.learning.service;

import com.medicology.learning.dto.request.SectionRequest;
import com.medicology.learning.dto.response.SectionResponse;
import com.medicology.learning.dto.response.SectionSummaryResponse;
import com.medicology.learning.entity.Course;
import com.medicology.learning.entity.Section;
import com.medicology.learning.repository.CourseRepository;
import com.medicology.learning.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final LessonService lessonService;

    public List<SectionSummaryResponse> getSectionsByCourse(UUID courseId) {
        return sectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    public SectionResponse getSectionById(UUID sectionId) {
        return sectionRepository.findById(sectionId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + sectionId));
    }

    public SectionResponse createSection(SectionRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + request.getCourseId()));
        Section section = Section.builder()
                .course(course)
                .name(request.getName())
                .slug(request.getSlug())
                .orderIndex(request.getOrderIndex())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .build();
        return mapToResponse(sectionRepository.save(section));
    }

    public SectionResponse updateSection(UUID id, SectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Section not found with ID: " + id));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + request.getCourseId()));
        section.setCourse(course);
        section.setName(request.getName());
        section.setSlug(request.getSlug());
        section.setOrderIndex(request.getOrderIndex());
        section.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        return mapToResponse(sectionRepository.save(section));
    }

    public void deleteSection(UUID id) {
        if (!sectionRepository.existsById(id)) {
            throw new IllegalArgumentException("Section not found with ID: " + id);
        }
        sectionRepository.deleteById(id);
    }

    public SectionResponse mapToResponse(Section section) {
        return SectionResponse.builder()
                .id(section.getId())
                .courseId(section.getCourse().getId())
                .name(section.getName())
                .slug(section.getSlug())
                .orderIndex(section.getOrderIndex())
                .estimatedDurationMinutes(section.getEstimatedDurationMinutes())
                .lessons(section.getLessons() != null ? section.getLessons().stream()
                        .map(lessonService::mapToSummaryResponse)
                        .collect(Collectors.toList()) : null)
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }

    public SectionSummaryResponse mapToSummaryResponse(Section section) {
        return SectionSummaryResponse.builder()
                .id(section.getId())
                .name(section.getName())
                .slug(section.getSlug())
                .orderIndex(section.getOrderIndex())
                .estimatedDurationMinutes(section.getEstimatedDurationMinutes())
                .lessons(section.getLessons() != null ? section.getLessons().stream()
                        .map(lessonService::mapToSummaryResponse)
                        .collect(Collectors.toList()) : null)
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}
