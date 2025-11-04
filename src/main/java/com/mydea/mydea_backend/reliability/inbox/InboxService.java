package com.mydea.mydea_backend.reliability.inbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InboxService {
    private final InboxEventRepository inboxes;

    /**
     * 제공자/이벤트ID 조합으로 중복을 방지하며 최초만 저장합니다.
     * 이미 존재하면 false, 신규 저장 시 true 반환.
     */
    @Transactional
    public boolean recordIfFirst(String provider, String providerEventId, String payloadJson) {
        return inboxes.findByProviderAndProviderEventId(provider, providerEventId)
                .map(e -> false)
                .orElseGet(() -> {
                    InboxEvent e = InboxEvent.builder()
                            .provider(provider)
                            .providerEventId(providerEventId)
                            .payload(payloadJson)
                            .build();
                    inboxes.save(e);
                    return true;
                });
    }
}
