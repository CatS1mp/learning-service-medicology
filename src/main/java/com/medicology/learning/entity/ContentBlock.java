package com.medicology.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "content_block",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_content_block_content_order", columnNames = {"content_id", "order_index"})
        },
        indexes = {
                @Index(name = "idx_content_block_content", columnList = "content_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContentBlockKind kind;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "is_gradable")
    private Boolean isGradable = Boolean.FALSE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
