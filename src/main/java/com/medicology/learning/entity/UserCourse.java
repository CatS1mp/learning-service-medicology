package com.medicology.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_course")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserCourseId.class) // Because composite PK
public class UserCourse {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "course_id")
    private UUID courseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;

    @Column(name = "quizzes_correct")
    private Integer quizzesCorrect = 0;

    @CreationTimestamp
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
