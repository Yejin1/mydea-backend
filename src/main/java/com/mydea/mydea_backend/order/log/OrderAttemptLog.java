package com.mydea.mydea_backend.order.log;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_attempt_log", indexes = {
        @Index(name = "idx_order_attempt_endpoint_time", columnList = "endpoint, created_at"),
        @Index(name = "idx_order_attempt_idem", columnList = "idempotency_key")
})
public class OrderAttemptLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;

    @Column(name = "endpoint", length = 64, nullable = false)
    private String endpoint;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_no", length = 32)
    private String orderNo;

    @Column(name = "request_hash", length = 64)
    private String requestHash;

    @Lob
    @Column(name = "request_snapshot")
    private String requestSnapshot;

    @Lob
    @Column(name = "response_snapshot")
    private String responseSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 16, nullable = false)
    private AttemptResult result;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
