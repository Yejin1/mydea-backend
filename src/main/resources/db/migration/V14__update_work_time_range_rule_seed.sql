-- 작업시간관리 규칙 변경 (사이즈 기준 반영)

DELETE FROM work_time_range_rule;

-- BASIC
INSERT INTO work_time_range_rule (product_type, size_min, size_max, min_per_unit, priority)
VALUES
('BASIC', 0,      60.00,  5, 10),
('BASIC', 60.01,  210.00, 10, 10),
('BASIC', 210.01, 430.00, 17, 10),
('BASIC', 430.01, 480.00, 21, 10);

-- FLOWER
INSERT INTO work_time_range_rule (product_type, size_min, size_max, min_per_unit, priority)
VALUES
('FLOWER', 0,      55.00,   8, 10),
('FLOWER', 55.01,  60.00,  10, 10),
('FLOWER', 60.01,  210.00, 19, 10),
('FLOWER', 210.01, 400.00, 23, 10),
('FLOWER', 400.01, 440.00, 26, 10),
('FLOWER', 440.01, 480.00, 29, 10);
