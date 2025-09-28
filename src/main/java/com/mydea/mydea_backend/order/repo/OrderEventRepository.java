package com.mydea.mydea_backend.order.repo;

import com.mydea.mydea_backend.order.domain.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> { }