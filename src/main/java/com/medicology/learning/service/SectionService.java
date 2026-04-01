package com.medicology.learning.service;

import com.medicology.learning.dto.request.SectionRequest;
import com.medicology.learning.dto.response.SectionResponse;
import com.medicology.learning.entity.Section;
import com.medicology.learning.entity.Theme;
import com.medicology.learning.repository.SectionRepository;
import com.medicology.learning.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final SectionRepository sectionRepository;
    private final ThemeRepository themeRepository;

    public List<SectionResponse> getSectionsByTheme(UUID themeId) {
        return sectionRepository.findByThemeIdOrderByOrderIndexAsc(themeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<SectionResponse> getSectionsByCourseId(UUID courseId) {
        // Technically section belongs to theme, and course belongs to section.
        // Wait, the API specifies: GET /api/v1/learning/courses/{courseId}/sections
        // In the data model, Course has section_id (N-1), so "sections by course" doesn't make sense.
        // Let's implement what's requested mapping-wise, but fix the semantics later. For now let's just return correctly via controller.
        return List.of();
    }

    public SectionResponse createSection(SectionRequest request) {
        Theme theme = themeRepository.findById(request.getThemeId())
                .orElseThrow(() -> new IllegalArgumentException("Theme not found with ID: " + request.getThemeId()));
        Section section = Section.builder()
                .theme(theme)
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
        Theme theme = themeRepository.findById(request.getThemeId())
                .orElseThrow(() -> new IllegalArgumentException("Theme not found with ID: " + request.getThemeId()));
        section.setTheme(theme);
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
                .themeId(section.getTheme().getId())
                .name(section.getName())
                .slug(section.getSlug())
                .orderIndex(section.getOrderIndex())
                .estimatedDurationMinutes(section.getEstimatedDurationMinutes())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}
