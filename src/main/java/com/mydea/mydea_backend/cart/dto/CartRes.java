package com.mydea.mydea_backend.cart.dto;

import java.util.List;

public record CartRes(
        List<CartItemRes> items,
        int total,   // 합계
        int count    // 아이템 개수(행 수)
) {}