package com.mydea.mydea_backend.cart.dto;

public record CartItemRes(
        Long cartItemId,
        Long workId,
        String optionHash,
        String name,
        String thumbUrl,
        int unitPrice,
        int quantity,
        int lineTotal   // unitPrice * quantity
) {}
