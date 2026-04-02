package com.medicology.learning.controller;

import com.medicology.learning.service.ProgressService;
import com.medicology.learning.wrapper.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/progress")
@RequiredArgsConstructor
public class ProgressController {
    private final ProgressService progressService;

    @GetMapping
    public ResponseEntity<?> getProgress(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(progressService.getUserProgress(user.getId()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProgressByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(progressService.getUserProgress(userId));
    }

    @PostMapping("/streak/ping")
    public ResponseEntity<?> pingStreak(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(progressService.updateStreak(user.getId()));
    }
}
