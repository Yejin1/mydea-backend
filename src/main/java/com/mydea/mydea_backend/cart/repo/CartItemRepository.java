package com.mydea.mydea_backend.cart.repo;

import com.mydea.mydea_backend.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndWorkIdAndOptionHash(Long cartId, Long workId, String optionHash);
    List<CartItem> findByCartId(Long cartId);
}