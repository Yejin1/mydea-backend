package com.mydea.mydea_backend.cart.controller;

import com.mydea.mydea_backend.cart.dto.AddItemReq;
import com.mydea.mydea_backend.cart.dto.CartRes;
import com.mydea.mydea_backend.cart.dto.MergeReq;
import com.mydea.mydea_backend.cart.dto.QuantityReq;
import com.mydea.mydea_backend.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
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

    @PatchMapping("/items/{itemId}")
    public void updateQty(@PathVariable Long itemId, @RequestBody QuantityReq req){
    }

    @DeleteMapping("/items/{itemId}")
    public void remove(@PathVariable Long itemId){
        // deleteById
    }

    @PostMapping("/merge")
    public CartRes merge(Authentication auth, @RequestBody MergeReq req){
        return cartService.merge(Long.valueOf(auth.getName()), req.items());
    }
}
