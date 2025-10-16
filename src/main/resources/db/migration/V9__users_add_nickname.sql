-- 계정 테이블에 닉네임 칼럼 추가
ALTER TABLE `users`
  ADD COLUMN `nickname` VARCHAR(50) NULL AFTER `name`;
