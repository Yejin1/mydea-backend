
-- 작업 소요 시간 범위 룰 테이블
CREATE TABLE work_time_range_rule (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_type  VARCHAR(30) NOT NULL,       -- 'BASIC' | 'FLOWER'
  size_min      DECIMAL(10,2) NOT NULL,     -- 포함
  size_max      DECIMAL(10,2) NOT NULL,     -- 포함
  min_per_unit  INT NOT NULL,               -- 1개당 작업 분
  priority      INT NOT NULL DEFAULT 100,   -- 겹칠 때 낮은 값 우선
  CONSTRAINT uk_range UNIQUE (product_type, size_min, size_max)
);

CREATE INDEX ix_range_lookup
ON work_time_range_rule(product_type, size_min, size_max, priority);

-- 슬롯(자원) 용량
CREATE TABLE work_slot (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  work_date     DATE NOT NULL,
  slot_index    INT  NOT NULL,         -- 0..N (예: 2시간 단위면 하루 12개)
  resource      VARCHAR(30) NOT NULL,  -- 'DESIGNER_1'
  capacity_min  INT NOT NULL,          -- 이 슬롯에서 처리 가능한 총 분
  reserved_min  INT NOT NULL DEFAULT 0,
  version       INT NOT NULL DEFAULT 0, -- 낙관락
  UNIQUE KEY uk_slot (work_date, slot_index, resource)
);
