package com.mydea.mydea_backend.production.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_slot", uniqueConstraints = @UniqueConstraint(name = "uk_slot", columnNames = { "work_date",
        "slot_index", "resource" }))
public class WorkSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex; // 0..N (하루 N개 슬롯)

    @Column(name = "resource", length = 30, nullable = false)
    private String resource; // 작업자/설비 식별자

    @Column(name = "capacity_min", nullable = false)
    private Integer capacityMin; // 이 슬롯의 총 처리 가능 분

    @Builder.Default
    @Column(name = "reserved_min", nullable = false)
    private Integer reservedMin = 0; // 예약된 분

    @Version
    @Column(name = "version", nullable = false)
    private Integer version; // 낙관적 락
}
