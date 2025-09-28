package com.mydea.mydea_backend.order.dto;

import lombok.*;
import java.util.List;

@Data @Builder
public class OrderPreviewResponse {
    private List<Item> items;
    private Integer subtotal;
    private Integer shippingFee;
    private Integer discount;
    private Integer total;

    @Data @Builder
    public static class Item {
        private Long workId;
        private String name;
        private String optionHash;
        private String thumbUrl;
        private Integer unitPrice;
        private Integer quantity;
        private Integer lineTotal;
    }
}
