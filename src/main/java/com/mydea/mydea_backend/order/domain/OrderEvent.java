package com.mydea.mydea_backend.order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "order_event", indexes = {
        @Index(name = "idx_order_event_order", columnList = "order_id, created_at")
})
public class OrderEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_event_id")
    private Long orderEventId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 32, nullable = false)
    private OrderStatus toStatus;

    @Column(name = "reason", length = 200)
    private String reason;

    @Lob
    @Column(name = "meta")
    private String meta; // JSON string

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}