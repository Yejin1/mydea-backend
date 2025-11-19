package com.mydea.mydea_backend.production.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "worker_info")
public class WorkerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "mon_min", nullable = false)
    private int monMin;
    @Column(name = "tue_min", nullable = false)
    private int tueMin;
    @Column(name = "wed_min", nullable = false)
    private int wedMin;
    @Column(name = "thu_min", nullable = false)
    private int thuMin;
    @Column(name = "fri_min", nullable = false)
    private int friMin;
    @Column(name = "sat_min", nullable = false)
    private int satMin;
    @Column(name = "sun_min", nullable = false)
    private int sunMin;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        } else {
            updatedAt = LocalDateTime.now();
        }
    }

    public int minutesFor(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> monMin;
            case TUESDAY -> tueMin;
            case WEDNESDAY -> wedMin;
            case THURSDAY -> thuMin;
            case FRIDAY -> friMin;
            case SATURDAY -> satMin;
            case SUNDAY -> sunMin;
        };
    }
}
