package com.medicology.learning.controller;

import com.medicology.learning.dto.common.ApiResponse;
import com.medicology.learning.dto.request.AiFeedbackCreateRequest;
import com.medicology.learning.dto.request.AiFeedbackUpdateRequest;
import com.medicology.learning.dto.response.AiFeedbackResponse;
import com.medicology.learning.entity.AiLearningFeedback;
import com.medicology.learning.service.AiFeedbackService;
import com.medicology.learning.wrapper.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning/ai-feedback")
@RequiredArgsConstructor
public class AiFeedbackController {
    private final AiFeedbackService aiFeedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<AiLearningFeedback>> requestFeedback(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody AiFeedbackCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(),
                "AI feedback created successfully",
                aiFeedbackService.generateFeedback(user.getId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AiFeedbackResponse>>> listFeedbacks(
            @AuthenticationPrincipal UserPrincipal user) {
        List<AiFeedbackResponse> list = user.isAdmin()
                ? aiFeedbackService.getAllFeedbacksAdmin()
                : aiFeedbackService.getFeedbacksForUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiFeedbackResponse>> updateFeedback(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody AiFeedbackUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "AI feedback updated successfully",
                aiFeedbackService.updateFeedback(id, user.getId(), user.isAdmin(), request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal user) {
        aiFeedbackService.deleteFeedback(id, user.getId(), user.isAdmin());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "AI feedback deleted successfully", null));
    }
}
