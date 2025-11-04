package com.mydea.mydea_backend.reliability.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRecordRepository records;

    /**
     * 아이덤포턴시 키로 레코드를 시작합니다. 기존에 존재하면 그대로 반환하고,
     * requestHash가 다르면 예외를 던집니다.
     */
    @Transactional
    public IdempotencyRecord begin(String key, String endpoint, Long userId, String requestHash, LocalDateTime ttl) {
        Optional<IdempotencyRecord> existing = records.findById(key);
        if (existing.isPresent()) {
            IdempotencyRecord r = existing.get();
            if (r.getRequestHash() != null && requestHash != null && !r.getRequestHash().equals(requestHash)) {
                throw new IllegalStateException("같은 Idempotency-Key에 다른 요청 본문이 전송되었습니다.");
            }
            return r;
        }
        IdempotencyRecord r = IdempotencyRecord.builder()
                .idempotencyKey(key)
                .endpoint(endpoint)
                .userId(userId)
                .requestHash(requestHash)
                .status(IdempotencyStatus.IN_PROGRESS)
                .expiresAt(ttl)
                .build();
        return records.save(r);
    }

    /**
     * 요청을 완료로 표시하고, 응답 스냅샷을 저장합니다.
     */
    @Transactional
    public void complete(String key, String responseJson) {
        IdempotencyRecord r = records.findById(key)
                .orElseThrow(() -> new IllegalStateException("Idempotency 레코드를 찾을 수 없습니다."));
        r.setStatus(IdempotencyStatus.COMPLETED);
        r.setResponseSnapshot(responseJson);
    }

    /**
     * 요청을 실패로 표시합니다(재시도 정책에 따라 사용할 수 있음).
     */
    @Transactional
    public void fail(String key, String responseJson) {
        IdempotencyRecord r = records.findById(key)
                .orElseThrow(() -> new IllegalStateException("Idempotency 레코드를 찾을 수 없습니다."));
        r.setStatus(IdempotencyStatus.FAILED);
        r.setResponseSnapshot(responseJson);
    }
}
