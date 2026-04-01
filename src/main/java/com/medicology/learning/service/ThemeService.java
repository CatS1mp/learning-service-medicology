package com.medicology.learning.service;

import com.medicology.learning.dto.request.ThemeRequest;
import com.medicology.learning.dto.response.ThemeResponse;
import com.medicology.learning.entity.Theme;
import com.medicology.learning.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;

    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ThemeResponse getThemeById(UUID id) {
        return themeRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new IllegalArgumentException("Theme not found with ID: " + id));
    }

    public ThemeResponse createTheme(ThemeRequest request) {
        Theme theme = Theme.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .iconFileName(request.getIconFileName())
                .colorCode(request.getColorCode())
                .orderIndex(request.getOrderIndex())
                .build();
        return mapToResponse(themeRepository.save(theme));
    }

    public ThemeResponse updateTheme(UUID id, ThemeRequest request) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Theme not found with ID: " + id));
        theme.setName(request.getName());
        theme.setSlug(request.getSlug());
        theme.setDescription(request.getDescription());
        theme.setIconFileName(request.getIconFileName());
        theme.setColorCode(request.getColorCode());
        theme.setOrderIndex(request.getOrderIndex());
        return mapToResponse(themeRepository.save(theme));
    }

    public void deleteTheme(UUID id) {
        if (!themeRepository.existsById(id)) {
            throw new IllegalArgumentException("Theme not found with ID: " + id);
        }
        themeRepository.deleteById(id);
    }

    private ThemeResponse mapToResponse(Theme theme) {
        return ThemeResponse.builder()
                .id(theme.getId())
                .name(theme.getName())
                .slug(theme.getSlug())
                .description(theme.getDescription())
                .iconFileName(theme.getIconFileName())
                .colorCode(theme.getColorCode())
                .orderIndex(theme.getOrderIndex())
                .createdAt(theme.getCreatedAt())
                .updatedAt(theme.getUpdatedAt())
                .build();
    }
}
