package com.mydea.mydea_backend.reliability.outbox;

public interface OutboxDispatcher {
    /**
     * 이벤트를 실제 외부 시스템으로 전송합니다. 성공 시 true 반환.
     */
    boolean dispatch(OutboxEvent event) throws Exception;
}
