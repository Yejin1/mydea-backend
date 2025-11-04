-- 결제 신뢰성 강화를 위한 Outbox / Inbox / Idempotency 테이블

-- 아웃박스 이벤트 테이블
CREATE TABLE IF NOT EXISTS outbox_event (
  outbox_id        BIGINT        PRIMARY KEY AUTO_INCREMENT, -- PK
  aggregate_type   VARCHAR(50)   NOT NULL,                   -- 어그리게이트 타입(도메인 주체)
  aggregate_id     BIGINT        NULL,                       -- 어그리게이트 ID(선택)
  event_type       VARCHAR(50)   NOT NULL,                   -- 이벤트 유형
  payload          JSON          NOT NULL,                   -- 이벤트 페이로드(JSON)
  status           ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING', -- 상태
  attempt_count    INT           NOT NULL DEFAULT 0,         -- 전송 시도 횟수
  next_attempt_at  DATETIME      NULL,                       -- 다음 전송 예정 시각
  created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 생성 시각
  last_error       TEXT          NULL,                       -- 마지막 오류 메시지
  INDEX idx_outbox_status_next_attempt (status, next_attempt_at),
  INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
);

-- 인박스 이벤트 테이블 (웹훅 멱등 처리)
CREATE TABLE IF NOT EXISTS inbox_event (
  inbox_id           BIGINT        PRIMARY KEY AUTO_INCREMENT, -- PK
  provider           VARCHAR(50)   NOT NULL,                   -- 프로바이더명(PSP 등)
  provider_event_id  VARCHAR(100)  NOT NULL,                   -- 외부 이벤트 ID(중복 방지)
  payload            JSON          NOT NULL,                   -- 웹훅 원문(JSON)
  received_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 수신 시각
  processed_at       DATETIME      NULL,                       -- 처리 완료 시각
  UNIQUE KEY uk_inbox_provider_event (provider, provider_event_id),
  INDEX idx_inbox_processed (processed_at)
);

-- 애플리케이션 엔드포인트 요청 멱등 저장소
CREATE TABLE IF NOT EXISTS idempotency_record (
  idempotency_key   VARCHAR(128)  NOT NULL, -- 아이덤포턴시 키(PK)
  endpoint          VARCHAR(128)  NOT NULL, -- 엔드포인트 식별자
  user_id           BIGINT        NULL,     -- 요청 사용자 ID
  request_hash      VARCHAR(64)   NULL,     -- 요청 본문 해시(SHA-256)
  status            ENUM('IN_PROGRESS','COMPLETED','FAILED') NOT NULL DEFAULT 'IN_PROGRESS', -- 상태
  response_snapshot JSON          NULL,     -- 응답 스냅샷(JSON)
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 생성 시각
  updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 수정 시각
  expires_at        DATETIME      NULL,     -- 만료 시각
  PRIMARY KEY (idempotency_key),
  INDEX idx_idem_endpoint_user (endpoint, user_id),
  INDEX idx_idem_expires (expires_at)
);

-- 4) 결제: PSP 측 중복 방지를 위한 provider idempotency key 추가
-- 일부 MySQL 8.x 환경에서 ALTER TABLE ... IF NOT EXISTS 지원 버전 차이로 실패할 수 있어
-- INFORMATION_SCHEMA 조회 + 동적 DDL로 안전하게 처리합니다.

-- 컬럼 존재 여부 확인 후 조건부 추가
SET @col_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'payment'
     AND COLUMN_NAME = 'provider_idempotency_key'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE payment ADD COLUMN provider_idempotency_key VARCHAR(64) NULL',
  'DO 0'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 유니크 인덱스(키) 존재 여부 확인 후 조건부 추가
SET @idx_exists := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'payment'
     AND INDEX_NAME = 'uk_payment_provider_idem'
);
SET @ddl2 := IF(@idx_exists = 0,
  'ALTER TABLE payment ADD UNIQUE KEY uk_payment_provider_idem (provider_idempotency_key)',
  'DO 0'
);
PREPARE stmt2 FROM @ddl2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;
