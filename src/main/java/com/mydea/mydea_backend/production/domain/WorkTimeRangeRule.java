package com.mydea.mydea_backend.production.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_time_range_rule", uniqueConstraints = @UniqueConstraint(name = "uk_range", columnNames = {
        "product_type", "size_min", "size_max" }), indexes = {
                @Index(name = "ix_range_lookup", columnList = "product_type, size_min, size_max, priority")
        })
public class WorkTimeRangeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 30, nullable = false)
    private ProductionProductType productType;

    @Column(name = "size_min", precision = 10, scale = 2, nullable = false)
    private BigDecimal sizeMin;

    @Column(name = "size_max", precision = 10, scale = 2, nullable = false)
    private BigDecimal sizeMax;

    @Column(name = "min_per_unit", nullable = false)
    private Integer minPerUnit;

    @Builder.Default
    @Column(name = "priority", nullable = false)
    private Integer priority = 100;
}
