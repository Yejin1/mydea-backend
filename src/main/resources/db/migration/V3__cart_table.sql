
CREATE TABLE IF NOT EXISTS cart (
  cart_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id      BIGINT NOT NULL UNIQUE,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS cart_item (
  cart_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cart_id      BIGINT NOT NULL,
  work_id      BIGINT NOT NULL,
  option_hash  VARCHAR(64) NOT NULL DEFAULT 'DEFAULT',
  name         VARCHAR(120) NOT NULL,
  thumb_url    VARCHAR(512) NOT NULL,
  unit_price   INT NOT NULL DEFAULT 0,
  quantity     INT NOT NULL DEFAULT 1,
  added_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_cart_work (cart_id, work_id, option_hash),
  CONSTRAINT fk_item_cart FOREIGN KEY (cart_id) REFERENCES cart(cart_id)
);
