package com.medicology.learning.service;

import com.medicology.learning.dto.request.AiFeedbackCreateRequest;
import com.medicology.learning.dto.request.AiFeedbackUpdateRequest;
import com.medicology.learning.dto.response.AiFeedbackResponse;
import com.medicology.learning.entity.AiLearningFeedback;
import com.medicology.learning.repository.AiLearningFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class AiFeedbackService {
    private final AiLearningFeedbackRepository aiFeedbackRepository;
    // Inject Spring AI ChatClient here when dependency is added
    // private final ChatClient chatClient; 

    public AiLearningFeedback generateFeedback(UUID userId, AiFeedbackCreateRequest request) {
        UUID referenceId = request.getReferenceId();
        String refType = request.getReferenceType();
        String question = request.getQuestionContent();
        String answer = request.getUserAnswer();
        boolean isCorrect = Boolean.TRUE.equals(request.getIsCorrect());
        
        // Tạo AI Prompt
        String prompt = String.format("Bạn là gia sư y khoa. Giải thích ngắn gọn tại sao câu trả lời '%s' cho câu hỏi '%s' là %s.", 
                                      answer, question, isCorrect ? "đúng" : "sai");
                                      
        // Giả lập AI call (Nếu có Spring AI: dùng chatClient.prompt(prompt).call().content())
        String aiResponse = "[Mock AI] Câu trả lời của bạn chưa đủ ý sách giáo khoa. Hãy xem lại chương Hệ Hô Hấp.";

        AiLearningFeedback feedback = AiLearningFeedback.builder()
            .userId(userId)
            .referenceId(referenceId)
            .referenceType(refType)
            .questionContent(question)
            .userAnswer(answer)
            .isCorrect(isCorrect)
            .aiExplanation(aiResponse)
            .build();
            
        return aiFeedbackRepository.save(feedback);
    }

    public List<AiFeedbackResponse> getFeedbacksForUser(UUID userId) {
        return aiFeedbackRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AiFeedbackResponse> getAllFeedbacksAdmin() {
        return aiFeedbackRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AiFeedbackResponse updateFeedback(UUID id, UUID userId, boolean admin, AiFeedbackUpdateRequest request) {
        AiLearningFeedback feedback = aiFeedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        if (!admin && !feedback.getUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "Not allowed to update this feedback");
        }
        feedback.setAiExplanation(request.getAiExplanation());
        feedback.setIsCorrect(request.getIsCorrect());
        return mapToResponse(aiFeedbackRepository.save(feedback));
    }

    public void deleteFeedback(UUID id, UUID userId, boolean admin) {
        AiLearningFeedback feedback = aiFeedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
        if (!admin && !feedback.getUserId().equals(userId)) {
            throw new ResponseStatusException(FORBIDDEN, "Not allowed to delete this feedback");
        }
        aiFeedbackRepository.delete(feedback);
    }

    private AiFeedbackResponse mapToResponse(AiLearningFeedback feedback) {
        return AiFeedbackResponse.builder()
                .id(feedback.getId())
                .userId(feedback.getUserId())
                .referenceId(feedback.getReferenceId())
                .referenceType(feedback.getReferenceType())
                .questionContent(feedback.getQuestionContent())
                .userAnswer(feedback.getUserAnswer())
                .isCorrect(feedback.getIsCorrect())
                .aiExplanation(feedback.getAiExplanation())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
