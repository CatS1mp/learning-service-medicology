package com.medicology.learning.controller;

import com.medicology.learning.dto.request.AssessmentResultSyncRequest;
import com.medicology.learning.repository.UserLessonRepository;
import com.medicology.learning.service.LessonService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/learning/internal")
@RequiredArgsConstructor
public class InternalLearningApiController {

    private static final Logger log = LoggerFactory.getLogger(InternalLearningApiController.class);

    private final UserLessonRepository userLessonRepository;
    private final LessonService lessonService;

    @Value("${internal.api.token:}")
    private String internalApiToken;

    @GetMapping("/assessment-access")
    public ResponseEntity<Void> checkAssessmentAccess(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestParam UUID userId,
            @RequestParam UUID sectionId,
            @RequestParam(required = false) UUID lessonId) {
        validateToken(token);
        boolean allowed = lessonId != null
                ? userLessonRepository.findByUserIdAndLessonId(userId, lessonId).isPresent()
                : userLessonRepository.existsByUserIdAndLessonSectionId(userId, sectionId);
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assessment-result")
    public ResponseEntity<Void> receiveAssessmentResult(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody AssessmentResultSyncRequest body) {
        validateToken(token);
        log.info(
                "assessment_result_sync userId={} courseId={} sectionId={} assessmentId={} attemptId={} score={} passed={} resultStatus={}",
                body.userId(),
                body.courseId(),
                body.sectionId(),
                body.assessmentId(),
                body.attemptId(),
                body.score(),
                body.passed(),
                body.resultStatus());
        lessonService.applyAssessmentResultSync(body);
        return ResponseEntity.accepted().build();
    }

    private void validateToken(String token) {
        if (internalApiToken == null || internalApiToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Internal API token not configured");
        }
        if (token == null || !internalApiToken.equals(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal token");
        }
    }
}
