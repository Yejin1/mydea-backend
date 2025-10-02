-- is_virtual 컬럼 추가
ALTER TABLE mydea.cart
  ADD COLUMN is_virtual TINYINT(1) NOT NULL DEFAULT 0 AFTER user_id;

-- 기존 UNIQUE(user_id) 제약 삭제
ALTER TABLE mydea.cart DROP KEY user_id;
ALTER TABLE mydea.cart DROP INDEX user_id;

-- 가상/정규 구분 위한 생성 컬럼 (정규 cart일 때만 1, 가상 cart면 NULL)
ALTER TABLE mydea.cart
  ADD COLUMN active_flag TINYINT(1)
    AS (CASE WHEN is_virtual = 0 THEN 1 ELSE NULL END);

-- 정규 cart 고유성 보장: (user_id, active_flag) UNIQUE
-- active_flag가 NULL이면 UNIQUE 비교 제외, 가상 cart는 무제한
CREATE UNIQUE INDEX ux_carts_user_active ON mydea.cart(user_id, active_flag);

-- 장바구니 만료시간 관리
ALTER TABLE mydea.cart
  ADD COLUMN expires_at DATETIME NULL AFTER is_virtual;