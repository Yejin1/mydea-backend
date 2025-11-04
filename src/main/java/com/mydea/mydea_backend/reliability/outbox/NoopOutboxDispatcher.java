package com.mydea.mydea_backend.reliability.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoopOutboxDispatcher implements OutboxDispatcher {
    @Override
    public boolean dispatch(OutboxEvent event) {
        // 실제 PSP 전송 로직은 이후 구현: 현재는 로깅 후 성공 처리
        log.info("[Outbox] noop dispatch: id={} type={} event={}", event.getId(), event.getAggregateType(),
                event.getEventType());
        return true;
    }
}
