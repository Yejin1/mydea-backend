package com.mydea.mydea_backend.cart.repo;

import com.mydea.mydea_backend.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndWorkIdAndOptionHash(Long cartId, Long workId, String optionHash);
    Optional<CartItem> findByCartItemIdAndCartId(Long cartItemId, Long cartId);
    List<CartItem> findByCartId(Long cartId);

    @Modifying
    @Transactional
    int deleteByCartIdAndCartItemId(Long cartId, Long cartItemId);

    @Modifying
    @Transactional
    int deleteByCartIdAndWorkIdAndOptionHash(Long cartId, Long workId, String optionHash);

    @Modifying
    @Transactional
    int deleteByCartId(Long cartId);


}