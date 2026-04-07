package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.request.AiFeedbackUpdateRequest;
import com.medicology.learning.dto.response.AiFeedbackResponse;
import com.medicology.learning.entity.AiLearningFeedback;
import com.medicology.learning.service.AiFeedbackService;
import com.medicology.learning.wrapper.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/learning/ai-feedback")
@RequiredArgsConstructor
public class AiFeedbackController {
    private final AiFeedbackService aiFeedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<AiLearningFeedback>> requestFeedback(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, Object> request) {
        
        UUID referenceId = UUID.fromString((String) request.get("referenceId"));
        String refType = (String) request.get("referenceType");
        String question = (String) request.get("questionContent");
        String answer = (String) request.get("userAnswer");
        boolean isCorrect = (Boolean) request.get("isCorrect");

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(),
                "AI feedback created successfully",
                aiFeedbackService.generateFeedback(user.getId(), referenceId, refType, question, answer, isCorrect)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiFeedbackResponse>>> getAllFeedbacks() {
        return ResponseEntity.ok(ApiResponse.success(aiFeedbackService.getAllFeedbacks()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiFeedbackResponse>> updateFeedback(@PathVariable UUID id, @RequestBody AiFeedbackUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "AI feedback updated successfully",
                aiFeedbackService.updateFeedback(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable UUID id) {
        aiFeedbackService.deleteFeedback(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "AI feedback deleted successfully", null));
    }
}
