package com.medicology.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_section_test")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserSectionTestId.class)
public class UserSectionTest {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "section_test_id")
    private UUID sectionTestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_test_id", insertable = false, updatable = false)
    private SectionTest sectionTest;

    @Column(name = "quizzes_correct")
    private Integer quizzesCorrect = 0;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    private Boolean passed = false;

    @CreationTimestamp
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
