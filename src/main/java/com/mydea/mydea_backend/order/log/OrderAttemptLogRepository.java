package com.mydea.mydea_backend.order.log;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAttemptLogRepository extends JpaRepository<OrderAttemptLog, Long> {
}
