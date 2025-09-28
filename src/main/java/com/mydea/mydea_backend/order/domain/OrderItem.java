package com.mydea.mydea_backend.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "order_item", indexes = {
        @Index(name = "idx_order_item_order", columnList = "order_id")
})
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "work_id", nullable = false)
    private Long workId;

    @Column(name = "option_hash", length = 512, nullable = false)
    private String optionHash;

    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Column(name = "thumb_url", length = 512)
    private String thumbUrl;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice; // KRW 정수

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false)
    private Integer lineTotal; // unitPrice * quantity
}