-- 주문/결제 시도 기록을 성공/실패와 무관하게 남기기 위한 로그 테이블
-- 목적: 트랜잭션 롤백과 무관하게 항상 보존(REQUIRES_NEW에서 기록)

CREATE TABLE IF NOT EXISTS `order_attempt_log` (
  `attempt_id`        BIGINT NOT NULL AUTO_INCREMENT,
  `endpoint`          VARCHAR(64)  NOT NULL,                        -- 예: orders:create, orders:pay
  `idempotency_key`   VARCHAR(64)  NULL,                            -- 요청 헤더에서 전달된 키(있을 경우)
  `account_id`        BIGINT       NULL,
  `order_id`          BIGINT       NULL,
  `order_no`          VARCHAR(32)  NULL,
  `request_hash`      VARCHAR(64)  NULL,                            -- 요청 바디 해시 등
  `request_snapshot`  TEXT         NULL,                            -- 요청 스냅샷(JSON)
  `response_snapshot` TEXT         NULL,                            -- 응답 스냅샷(JSON)
  `result`            VARCHAR(16)  NOT NULL,                        -- PENDING | SUCCESS | FAIL
  `error_message`     TEXT         NULL,
  `created_at`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at`      TIMESTAMP    NULL,
  PRIMARY KEY (`attempt_id`),
  KEY `idx_order_attempt_endpoint_time` (`endpoint`, `created_at`),
  KEY `idx_order_attempt_idem` (`idempotency_key`)
);
