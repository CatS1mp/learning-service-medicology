package com.medicology.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_learning_feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiLearningFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "reference_type", length = 20)
    private String referenceType;

    @Column(name = "question_content", columnDefinition = "TEXT")
    private String questionContent;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
