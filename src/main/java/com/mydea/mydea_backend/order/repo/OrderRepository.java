package com.mydea.mydea_backend.order.repo;


import com.mydea.mydea_backend.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    Optional<Order> findByIdempotencyKey(String idempotencyKey);
    Page<Order> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}