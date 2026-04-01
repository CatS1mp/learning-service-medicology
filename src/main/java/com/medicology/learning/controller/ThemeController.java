package com.medicology.learning.controller;

import com.medicology.learning.dto.request.ThemeRequest;
import com.medicology.learning.dto.response.ThemeResponse;
import com.medicology.learning.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/themes")
@RequiredArgsConstructor
public class ThemeController {
    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        return ResponseEntity.ok(themeService.getAllThemes());
    }

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody ThemeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(themeService.createTheme(request));
    }

    @PutMapping("/{themeId}")
    public ResponseEntity<ThemeResponse> updateTheme(@PathVariable UUID themeId, @RequestBody ThemeRequest request) {
        return ResponseEntity.ok(themeService.updateTheme(themeId, request));
    }

    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable UUID themeId) {
        themeService.deleteTheme(themeId);
        return ResponseEntity.noContent().build();
    }
}
