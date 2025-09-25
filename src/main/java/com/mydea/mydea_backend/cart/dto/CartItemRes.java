package com.mydea.mydea_backend.cart.dto;

public record CartItemRes(
        Long itemId,
        Long workId,
        String optionHash,
        String name,
        String thumbUrl,
        int unitPrice,
        int quantity,
        int lineTotal   // unitPrice * quantity
) {}
