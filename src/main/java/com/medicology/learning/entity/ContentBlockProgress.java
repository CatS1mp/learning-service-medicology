package com.medicology.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "content_block_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_content_block_progress_attempt_block", columnNames = {"attempt_id", "content_block_id"})
        },
        indexes = {
                @Index(name = "idx_content_block_progress_block", columnList = "content_block_id"),
                @Index(name = "idx_content_block_progress_attempt", columnList = "attempt_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentBlockProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "answer_id")
    private UUID answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_block_id", nullable = false)
    private ContentBlock contentBlock;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
