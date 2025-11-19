-- 일별 작업 가능 용량 테이블
CREATE TABLE IF NOT EXISTS work_capacity_day (
  work_date DATE PRIMARY KEY,
  capacity_min INT NOT NULL,
  reserved_min INT NOT NULL DEFAULT 0,
  backlog_accepted_count INT NOT NULL DEFAULT 0,
  version INT NOT NULL DEFAULT 0
);
