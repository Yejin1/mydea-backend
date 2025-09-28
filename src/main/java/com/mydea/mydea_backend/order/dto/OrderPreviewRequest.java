package com.mydea.mydea_backend.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderPreviewRequest {
    @NotNull
    private Long cartId;
}