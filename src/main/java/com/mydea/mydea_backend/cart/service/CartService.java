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

    // 장바구니 아이템 추가
    @Transactional
    public void addItem(Long userId, AddItemReq req) {
        Cart cart = cartRepository.findByUserIdAndVirtualCartFalse(userId)
                .orElseGet(() -> cartRepository.save(newRegularCart(userId)));

        CartItem item = itemRepository
                .findByCartIdAndWorkIdAndOptionHash(cart.getCartId(), req.workId(), req.optionHash())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + req.quantity());
                    return existing;
                })
                .orElseGet(() -> toEntity(cart.getCartId(), req));

        itemRepository.save(item);
    }

    // 장바구니 목록 조회
    @Transactional
    public CartRes getCart(Long userId) {
        Cart cart = cartRepository.findByUserIdAndVirtualCartFalse(userId).orElse(null);
        if (cart == null)
            return new CartRes(null, List.of(), 0, 0);

        List<CartItem> items = itemRepository.findByCartId(cart.getCartId());
        if (items.isEmpty()) {
            return new CartRes(cart.getCartId(), List.of(), 0, 0);
        }
        return CartMapper.toCartRes(cart.getCartId(), items);
    }

    // 장바구니
    @Transactional
    public CartRes merge(Long userId, List<AddItemReq> localItems) {
        for (AddItemReq req : localItems)
            addItem(userId, req);
        return getCart(userId);
    }

    private Cart newRegularCart(Long userId) {
        return Cart.builder().userId(userId).virtualCart(false).build();
    }

    private CartItem toEntity(Long cartId, AddItemReq r) {
        CartItem i = new CartItem();
        i.setCartId(cartId);
        i.setWorkId(r.workId());
        i.setOptionHash(r.optionHash());
        i.setName(r.name());
        i.setThumbUrl(r.thumbUrl());
        i.setUnitPrice(r.unitPrice());
        i.setQuantity(r.quantity());
        return i;
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserIdAndVirtualCartFalse(userId)
                .orElseThrow(() -> new NotFoundException("유저 아이디 없음 : " + userId));

        // 삭제
        int affected = itemRepository.deleteByCartIdAndCartItemId(cart.getCartId(), itemId);
        if (affected == 0) {
            // itemId가 없거나 남의 장바구니인 경우 익셉션
            throw new NotFoundException("Cart item not found: " + itemId);
        }

    }

    /**
     * 장바구니 전체 비우기
     */
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserIdAndVirtualCartFalse(userId)
                .orElseThrow(() -> new NotFoundException("유저 아이디 없음 : " + userId));

        itemRepository.deleteByCartId(cart.getCartId());
    }

    @Transactional
    public void updateQuantity(Long userId, Long itemId, int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("수량이 0보다 커야함");

        // 본인 cart 조회 (없으면 에러)
        Cart cart = cartRepository.findByUserIdAndVirtualCartFalse(userId)
                .orElseThrow(() -> new NotFoundException("유저 아이디 없음 : " + userId));

        // 소유 검증 포함 조회
        CartItem item = itemRepository.findByCartItemIdAndCartId(itemId, cart.getCartId())
                .orElseThrow(() -> new NotFoundException("장바구니 아이템 없음 : " + itemId));

        // 0이면 삭제. 그 외에는 업데이트
        if (quantity == 0) {
            itemRepository.delete(item);
            return;
        }

        item.setQuantity(quantity);
        // 필요 시 updatedAt 갱신 컬럼 있으면 같이 갱신
        itemRepository.save(item);
    }

    /**
     * 바로 주문하기 등 일회성(가상) 장바구니 생성
     * 
     * @param userId     사용자
     * @param items      초기 아이템 목록
     * @param ttlSeconds 만료 TTL (null 또는 <=0 이면 만료 시간 없음)
     * @return 생성된 virtual cartId
     */
    @Transactional
    public Long createVirtualCart(Long userId, List<AddItemReq> items, Long ttlSeconds) {
        Cart v = Cart.builder()
                .userId(userId)
                .virtualCart(true)
                .expiresAt(ttlSeconds != null && ttlSeconds > 0 ? java.time.LocalDateTime.now().plusSeconds(ttlSeconds)
                        : null)
                .build();
        v = cartRepository.save(v);

        Long cartId = v.getCartId();
        for (AddItemReq r : items) {
            CartItem i = toEntity(cartId, r);
            itemRepository.save(i);
        }
        return cartId;
    }

    /** 간단한 만료 가상 카트 정리 로직 (비동기/스케쥴링 후보) */
    // public int cleanupExpiredVirtualCarts() { ... }
}
