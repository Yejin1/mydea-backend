package com.mydea.mydea_backend.reliability.idempotency;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "idempotency_record", indexes = {
        @Index(name = "idx_idem_endpoint_user", columnList = "endpoint, user_id"),
        @Index(name = "idx_idem_expires", columnList = "expires_at")
})
public class IdempotencyRecord {
    @Id
    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "endpoint", length = 128, nullable = false)
    private String endpoint;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "request_hash", length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private IdempotencyStatus status;

    @Column(name = "response_snapshot", columnDefinition = "json")
    private String responseSnapshot;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    void prePersist() {
        if (status == null)
            status = IdempotencyStatus.IN_PROGRESS;
    }
}
