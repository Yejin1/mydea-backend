
-- users (계정) 테이블
CREATE TABLE IF NOT EXISTS users (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  email           VARCHAR(255) NOT NULL UNIQUE,
  password_hash   VARCHAR(255) NOT NULL,
  name            VARCHAR(100) NOT NULL,
  phone           VARCHAR(30),
  role            VARCHAR(20) NOT NULL DEFAULT 'USER',
  status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  last_login_at   TIMESTAMP NULL,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- works (작업물) 테이블
CREATE TABLE IF NOT EXISTS works (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id          BIGINT NULL,
  name             VARCHAR(200) NULL,
  work_type        ENUM('ring','bracelet','necklace') NOT NULL,
  design_type      ENUM('basic','flower') NOT NULL DEFAULT 'basic',
  colors           JSON NOT NULL,
  flower_petal     CHAR(7) NULL,
  flower_center    CHAR(7) NULL,
  auto_size        INT NOT NULL DEFAULT 0,
  radius_mm        DECIMAL(8,3) NULL,
  size_index       INT NULL,
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_works_user (user_id),
  INDEX idx_works_type (work_type),
  CONSTRAINT fk_works_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
