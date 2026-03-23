package com.medicology.learning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "section_test")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionTest {
    @Id
    @Column(name = "section_id")
    private UUID sectionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "section_id")
    private Section section;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "passing_score_percentage", precision = 5, scale = 2)
    private BigDecimal passingScorePercentage = new BigDecimal("70.00");

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes = 30;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // JSON content
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
