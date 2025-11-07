package com.mydea.mydea_backend.cart.dto;

import com.mydea.mydea_backend.cart.domain.CartItem;
import java.util.List;

public final class CartMapper {
    private CartMapper() {
    }

    public static CartItemRes toRes(CartItem e) {
        int line = (e.getUnitPrice() == null ? 0 : e.getUnitPrice()) * (e.getQuantity() == null ? 0 : e.getQuantity());
        return new CartItemRes(
                e.getCartItemId(),
                e.getWorkId(),
                e.getOptionHash(),
                e.getName(),
                e.getThumbUrl(),
                e.getUnitPrice() == null ? 0 : e.getUnitPrice(),
                e.getQuantity() == null ? 0 : e.getQuantity(),
                line);
    }

    public static CartRes toCartRes(Long cartId, List<CartItem> items) {
        var itemDtos = items.stream().map(CartMapper::toRes).toList();
        int total = itemDtos.stream().mapToInt(CartItemRes::lineTotal).sum();
        return new CartRes(cartId, itemDtos, total, itemDtos.size());
    }
}
