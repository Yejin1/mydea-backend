package com.mydea.mydea_backend.cart.repo;

import com.mydea.mydea_backend.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    /** 기존 1:1 정규 장바구니(virtual=false) 조회 */
    Optional<Cart> findByUserIdAndVirtualCartFalse(Long userId);

    /** 기존 코드 호환 (정규 카트) */
    default Optional<Cart> findByUserId(Long userId) {
        return findByUserIdAndVirtualCartFalse(userId);
    }
}