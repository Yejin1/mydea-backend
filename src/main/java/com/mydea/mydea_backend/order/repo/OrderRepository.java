package com.mydea.mydea_backend.order.repo;

import com.mydea.mydea_backend.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    /**
     * 주문 목록 조회 시 아이템 컬렉션을 함께 로딩하여 N+1 완화
     * 컬렉션 fetch join은 페이지네이션과 궁합이 좋지 않으므로 EntityGraph + 배치페치로 처리
     */
    @EntityGraph(attributePaths = { "items" })
    Page<Order> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}