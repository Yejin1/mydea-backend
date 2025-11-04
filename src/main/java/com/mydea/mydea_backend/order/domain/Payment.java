package com.mydea.mydea_backend.order.domain;

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
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_id")
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private PaymentStatus status;

    @Column(name = "method", length = 32)
    private String method; // CARD, VBANK, SIMULATOR

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "provider", length = 32)
    private String provider;

    @Column(name = "provider_tx_id", length = 64)
    private String providerTxId;

    @Column(name = "provider_idempotency_key", length = 64)
    private String providerIdempotencyKey;

    @Lob
    @Column(name = "raw_callback")
    private String rawCallback;

    private LocalDateTime approvedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}