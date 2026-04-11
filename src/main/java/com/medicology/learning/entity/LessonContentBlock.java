package com.medicology.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "lesson_content_block",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_lesson_content_block_lesson_order", columnNames = {"lesson_id", "order_index"})
        },
        indexes = {
                @Index(name = "idx_lesson_content_block_lesson", columnList = "lesson_id"),
                @Index(name = "idx_lesson_content_block_assessment", columnList = "assessment_id"),
                @Index(name = "idx_lesson_content_block_question", columnList = "question_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonContentBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LessonContentBlockKind kind;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "question_id")
    private UUID questionId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
