-- 작업 소요 시간 범위 룰 데이터 시드
INSERT INTO work_time_range_rule (product_type, size_min, size_max, min_per_unit, priority)
VALUES
-- BASIC
('BASIC',  10.00, 14.99, 5, 10),
('BASIC',  15.00, 19.99, 8, 10),
('BASIC',  20.00, 25.00, 10, 10),

-- FLOWER
('FLOWER', 10.00, 14.99, 10, 10),
('FLOWER', 15.00, 19.99, 14, 10),
('FLOWER', 20.00, 25.00, 18, 10);
