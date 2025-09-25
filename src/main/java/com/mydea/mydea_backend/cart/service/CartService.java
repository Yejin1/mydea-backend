package com.mydea.mydea_backend.cart.service;

import com.mydea.mydea_backend.cart.domain.Cart;
import com.mydea.mydea_backend.cart.domain.CartItem;
import com.mydea.mydea_backend.cart.dto.AddItemReq;
import com.mydea.mydea_backend.cart.dto.CartMapper;
import com.mydea.mydea_backend.cart.dto.CartRes;
import com.mydea.mydea_backend.cart.repo.CartItemRepository;
import com.mydea.mydea_backend.cart.repo.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;

    @Transactional
    public void addItem(Long userId, AddItemReq req) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(newCart(userId)));

        CartItem item = itemRepository
                .findByCartIdAndWorkIdAndOptionHash(cart.getCartId(), req.workId(), req.optionHash())
                .map(existing -> { existing.setQuantity(existing.getQuantity() + req.quantity()); return existing; })
                .orElseGet(() -> toEntity(cart.getCartId(), req));

        itemRepository.save(item);
    }

    @Transactional(readOnly=true)
    public CartRes getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(newCart(userId)));
        List<CartItem> items = itemRepository.findByCartId(cart.getCartId());
        int total = items.stream().mapToInt(i -> i.getUnitPrice() * i.getQuantity()).sum();
        return CartMapper.toCartRes(items);
    }

    @Transactional
    public CartRes merge(Long userId, List<AddItemReq> localItems) {
        for (AddItemReq req : localItems) addItem(userId, req);
        return getCart(userId);
    }

    private Cart newCart(Long userId){ Cart c=new Cart(); c.setUserId(userId); return c; }
    private CartItem toEntity(Long cartId, AddItemReq r){
        CartItem i=new CartItem();
        i.setCartId(cartId); i.setWorkId(r.workId()); i.setOptionHash(r.optionHash());
        i.setName(r.name()); i.setThumbUrl(r.thumbUrl()); i.setUnitPrice(r.unitPrice()); i.setQuantity(r.quantity());
        return i;
    }
}
