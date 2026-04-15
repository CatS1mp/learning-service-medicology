package com.medicology.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_lesson_block_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_lesson_block_progress_user_block", columnNames = {"user_id", "lesson_content_block_id"})
        },
        indexes = {
                @Index(name = "idx_user_lesson_block_progress_user", columnList = "user_id"),
                @Index(name = "idx_user_lesson_block_progress_lesson", columnList = "lesson_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLessonBlockProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_content_block_id", nullable = false)
    private LessonContentBlock lessonContentBlock;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlockProgressStatus status = BlockProgressStatus.NOT_STARTED;

    @Column(name = "score")
    private Integer score;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "attempt_id")
    private UUID attemptId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_status", nullable = false, length = 20, columnDefinition = "varchar(20) default 'NOT_GRADED'")
    private AssessmentGradingStatus gradingStatus = AssessmentGradingStatus.NOT_GRADED;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
