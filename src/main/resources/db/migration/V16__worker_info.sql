-- 작업자 정보 테이블
CREATE TABLE IF NOT EXISTS worker_info (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(50) NOT NULL,
  is_active     BOOLEAN NOT NULL DEFAULT TRUE,
  mon_min       INT NOT NULL DEFAULT 0,
  tue_min       INT NOT NULL DEFAULT 0,
  wed_min       INT NOT NULL DEFAULT 0,
  thu_min       INT NOT NULL DEFAULT 0,
  fri_min       INT NOT NULL DEFAULT 0,
  sat_min       INT NOT NULL DEFAULT 0,
  sun_min       INT NOT NULL DEFAULT 0,
  updated_at    DATETIME NOT NULL
);

-- 초기 작업자 정보 데이터
INSERT INTO worker_info
(name, is_active, mon_min, tue_min, wed_min, thu_min, fri_min, sat_min, sun_min, updated_at)
VALUES
-- 포치타: 주중 풀타임
('Pochita', TRUE,
  400, 400, 400, 400, 400,
    0,   0,
  NOW()),

-- 아냐: 주중 풀타임
('Anya', TRUE,
  400, 400, 400, 400, 400,
    0,   0,
  NOW()),

-- 데스몬드: 주말 전담
('Desmond', TRUE,
    0,   0,   0,   0,   0,
  480, 480,
  NOW());
