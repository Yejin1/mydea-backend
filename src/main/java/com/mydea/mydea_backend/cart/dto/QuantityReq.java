package com.mydea.mydea_backend.cart.dto;

import jakarta.validation.constraints.Min;

public record QuantityReq(
        @Min(1) int quantity
) {}