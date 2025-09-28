package com.mydea.mydea_backend.order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_account_created", columnList = "account_id, created_at"),
        @Index(name = "idx_orders_status_created", columnList = "status, created_at"),
        @Index(name = "uq_orders_idemkey", columnList = "idempotency_key", unique = true)
})
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_no", length = 32, nullable = false, unique = true)
    private String orderNo;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private OrderStatus status;

    @Column(name = "subtotal_amount", nullable = false)
    private Integer subtotalAmount; // KRW 정수

    @Column(name = "shipping_fee", nullable = false)
    private Integer shippingFee; // KRW 정수

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount; // 기본 0

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount; // KRW 정수

    // 수령인/배송지 스냅샷
    @Column(name = "recipient_name", length = 60, nullable = false)
    private String recipientName;

    @Column(name = "phone", length = 40, nullable = false)
    private String phone;

    @Column(name = "address1", length = 200, nullable = false)
    private String address1;

    @Column(name = "address2", length = 200)
    private String address2;

    @Column(name = "zipcode", length = 16, nullable = false)
    private String zipcode;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    // 마일스톤
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime canceledAt;
    private LocalDateTime expiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
    }
}