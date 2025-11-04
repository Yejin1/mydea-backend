package com.mydea.mydea_backend.reliability.outbox;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("select o from OutboxEvent o where o.status = :status and (o.nextAttemptAt is null or o.nextAttemptAt <= :now) order by o.nextAttemptAt asc nulls first, o.id asc")
    Page<OutboxEvent> findPending(@Param("status") OutboxStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OutboxEvent> findWithLockingById(Long id);
}
