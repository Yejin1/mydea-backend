package com.mydea.mydea_backend.reliability.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxEventRepository outboxes;
    private final OutboxDispatcher dispatcher;

    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void pollAndPublish() {
        var page = outboxes.findPending(OutboxStatus.PENDING, LocalDateTime.now(), PageRequest.of(0, 20));
        page.forEach(e -> {
            try {
                processOne(e.getId());
            } catch (Exception ex) {
                log.warn("Outbox processing failed id={}: {}", e.getId(), ex.getMessage());
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(Long id) {
        outboxes.findWithLockingById(id).ifPresent(e -> {
            if (e.getStatus() != OutboxStatus.PENDING)
                return;
            try {
                boolean ok = dispatcher.dispatch(e);
                if (ok) {
                    e.setStatus(OutboxStatus.SENT);
                    e.setLastError(null);
                } else {
                    failAndBackoff(e, null);
                }
            } catch (Exception ex) {
                failAndBackoff(e, ex.getMessage());
            }
        });
    }

    private void failAndBackoff(OutboxEvent e, String error) {
        int attempts = e.getAttemptCount() == null ? 0 : e.getAttemptCount();
        e.setAttemptCount(attempts + 1);
        e.setLastError(error);
        // 간단한 선형 백오프: 10s * attempts
        e.setNextAttemptAt(LocalDateTime.now().plusSeconds(10L * (attempts + 1)));
        // 장기 실패 기준(예: 10회) 시 FAILED로 전환
        if (attempts + 1 >= 10) {
            e.setStatus(OutboxStatus.FAILED);
        }
    }
}
