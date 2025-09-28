package com.mydea.mydea_backend.order.dto;

import com.mydea.mydea_backend.order.domain.OrderStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class OrderResponse {
    private Long orderId;
    private String orderNo;
    private OrderStatus status;
    private Integer subtotal;
    private Integer shippingFee;
    private Integer discount;
    private Integer total;

    private String recipientName;
    private String phone;
    private String address1;
    private String address2;
    private String zipcode;

    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime canceledAt;

    private List<Item> items;

    @Data @Builder
    public static class Item {
        private Long orderItemId;
        private Long workId;
        private String name;
        private String optionHash;
        private String thumbUrl;
        private Integer unitPrice;
        private Integer quantity;
        private Integer lineTotal;
    }
}