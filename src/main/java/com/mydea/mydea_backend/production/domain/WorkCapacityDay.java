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
@Table(name = "work_capacity_day")
public class WorkCapacityDay {
    @Id
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "capacity_min", nullable = false)
    private Integer capacityMin;

    @Column(name = "reserved_min", nullable = false)
    private Integer reservedMin;

    @Column(name = "backlog_accepted_count", nullable = false)
    private Integer backlogAcceptedCount;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    public int remainingMinutes() {
        int cap = capacityMin == null ? 0 : capacityMin;
        int res = reservedMin == null ? 0 : reservedMin;
        return Math.max(0, cap - res);
    }
}
