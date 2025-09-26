package com.mydea.mydea_backend.cart.dto;

import java.util.List;

public record CartRes(
        List<CartItemRes> items,
        int total,
        int count
) {}