package com.medicology.learning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "content_block_template",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_content_block_template_kind_name", columnNames = {"kind", "name"})
        },
        indexes = {
                @Index(name = "idx_content_block_template_kind", columnList = "kind"),
                @Index(name = "idx_content_block_template_active", columnList = "is_active")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentBlockTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContentBlockKind kind;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "payload_schema_json", nullable = false, columnDefinition = "TEXT")
    private String payloadSchemaJson;

    @Column(name = "starter_payload_json", nullable = false, columnDefinition = "TEXT")
    private String starterPayloadJson;

    @Column(name = "default_is_gradable", nullable = false)
    @Builder.Default
    private Boolean defaultIsGradable = Boolean.FALSE;

    @Column(name = "default_max_score")
    private Integer defaultMaxScore;

    @Column(name = "allow_score_edit", nullable = false)
    @Builder.Default
    private Boolean allowScoreEdit = Boolean.TRUE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = Boolean.TRUE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
