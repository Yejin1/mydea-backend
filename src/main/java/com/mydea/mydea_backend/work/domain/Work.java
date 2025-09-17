package com.mydea.mydea_backend.work.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "works",
        indexes = {
                @Index(name = "idx_works_user", columnList = "user_id"),
                @Index(name = "idx_works_type", columnList = "work_type")
        })
public class Work {

    public enum WorkType { ring, bracelet, necklace }
    public enum DesignType { basic, flower }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String name;

    @Column(name = "preview_url", length = 500)
    private String previewUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", nullable = false, length = 16)
    private WorkType workType;

    @Enumerated(EnumType.STRING)
    @Column(name = "design_type", nullable = false, length = 16)
    private DesignType designType = DesignType.basic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json", nullable = false)
    private List<String> colors;

    @Column(name = "flower_petal", length = 7)
    private String flowerPetal;

    @Column(name = "flower_center", length = 7)
    private String flowerCenter;

    @Column(name = "auto_size", nullable = false)
    private Integer autoSize = 0;

    // 새 필드들
    @Column(name = "radius_mm", precision = 8, scale = 3)
    private BigDecimal radiusMm;          // 내경 반지름(mm)

    @Column(name = "size_index")
    private Integer sizeIndex;            // 옵션 선택 인덱스

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "timestamp default current_timestamp")
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "timestamp default current_timestamp on update current_timestamp")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }
    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }
}