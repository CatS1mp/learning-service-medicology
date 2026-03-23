package com.medicology.learning.controller;

import com.medicology.learning.entity.AiLearningFeedback;
import com.medicology.learning.service.AiFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/learning/ai-feedback")
@RequiredArgsConstructor
public class AiFeedbackController {
    private final AiFeedbackService aiFeedbackService;

    private UUID getUserId(String email) {
        return UUID.nameUUIDFromBytes(email.getBytes());
    }

    @PostMapping
    public ResponseEntity<AiLearningFeedback> requestFeedback(
            @AuthenticationPrincipal String email,
            @RequestBody Map<String, Object> request) {
        
        UUID referenceId = UUID.fromString((String) request.get("referenceId"));
        String refType = (String) request.get("referenceType");
        String question = (String) request.get("questionContent");
        String answer = (String) request.get("userAnswer");
        boolean isCorrect = (Boolean) request.get("isCorrect");

        return ResponseEntity.ok(aiFeedbackService.generateFeedback(
            getUserId(email), referenceId, refType, question, answer, isCorrect
        ));
    }
}
