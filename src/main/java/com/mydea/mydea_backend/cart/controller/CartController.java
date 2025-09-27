package com.mydea.mydea_backend.cart.controller;

import com.mydea.mydea_backend.cart.dto.AddItemReq;
import com.mydea.mydea_backend.cart.dto.CartRes;
import com.mydea.mydea_backend.cart.dto.MergeReq;
import com.mydea.mydea_backend.cart.dto.QuantityReq;
import com.mydea.mydea_backend.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public CartRes getCart(Authentication auth){
        return cartService.getCart(Long.valueOf(auth.getName()));
    }

    @PostMapping("/items")
    public void add(Authentication auth, @RequestBody AddItemReq req){
        cartService.addItem(Long.valueOf(auth.getName()), req);
    }

    //장바구니 아이템 수량 변경 (미구현)
    @PatchMapping("/items/{itemId}")
    public void updateQty(Authentication auth, @PathVariable Long itemId, @RequestBody QuantityReq req){
        cartService.updateQuantity(Long.valueOf(auth.getName()), itemId, req.quantity());
    }

    //장바구니 아이템 삭제
    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication auth, @PathVariable Long itemId){
        cartService.removeItem(Long.valueOf(auth.getName()), itemId);
    }
    
    // 장바구니 전체 삭제
    @DeleteMapping("/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(Authentication auth) {
        cartService.clearCart(Long.valueOf(auth.getName()));
    }

    @PostMapping("/merge")
    public CartRes merge(Authentication auth, @RequestBody MergeReq req){
        return cartService.merge(Long.valueOf(auth.getName()), req.items());
    }
}
