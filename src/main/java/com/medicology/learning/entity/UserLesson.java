package com.medicology.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_lesson")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserLessonId.class)
public class UserLesson {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "lesson_id")
    private UUID lessonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", insertable = false, updatable = false)
    private Lesson lesson;

    @Column(name = "quizzes_correct")
    private Integer quizzesCorrect = 0;

    @CreationTimestamp
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
