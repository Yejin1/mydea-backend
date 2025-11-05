package com.mydea.mydea_backend.order.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderAttemptLogService {
    private final OrderAttemptLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long start(String endpoint,
            String idempotencyKey,
            Long accountId,
            String orderNo,
            Long orderId,
            String requestHash,
            String requestSnapshot) {
        OrderAttemptLog log = OrderAttemptLog.builder()
                .endpoint(endpoint)
                .idempotencyKey(idempotencyKey)
                .accountId(accountId)
                .orderNo(orderNo)
                .orderId(orderId)
                .requestHash(requestHash)
                .requestSnapshot(requestSnapshot)
                .result(AttemptResult.PENDING)
                .build();
        return repository.save(log).getAttemptId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(Long attemptId, String responseSnapshot) {
        repository.findById(attemptId).ifPresent(log -> {
            log.setResult(AttemptResult.SUCCESS);
            log.setResponseSnapshot(responseSnapshot);
            log.setCompletedAt(LocalDateTime.now());
            repository.save(log);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long attemptId, String errorMessage, String responseSnapshot) {
        repository.findById(attemptId).ifPresent(log -> {
            log.setResult(AttemptResult.FAIL);
            log.setErrorMessage(errorMessage);
            if (responseSnapshot != null) {
                log.setResponseSnapshot(responseSnapshot);
            }
            log.setCompletedAt(LocalDateTime.now());
            repository.save(log);
        });
    }
}
