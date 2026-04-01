package com.medicology.learning.controller;

import com.medicology.learning.service.ProgressService;
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

    private UUID getUserId(String email) {
        return UUID.nameUUIDFromBytes(email.getBytes());
    }

    @GetMapping
    public ResponseEntity<?> getProgress(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(progressService.getUserProgress(getUserId(email)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProgressByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(progressService.getUserProgress(userId));
    }

    @PostMapping("/streak/ping")
    public ResponseEntity<?> pingStreak(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(progressService.updateStreak(getUserId(email)));
    }
}
