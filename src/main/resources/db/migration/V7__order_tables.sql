
-- ORDERS
CREATE TABLE IF NOT EXISTS `orders` (
  `order_id`        BIGINT NOT NULL AUTO_INCREMENT,
  `order_no`        VARCHAR(32)  NOT NULL,
  `account_id`      BIGINT       NOT NULL,
  `status`          VARCHAR(32)  NOT NULL,
  `subtotal_amount` INT          NOT NULL,
  `shipping_fee`    INT          NOT NULL,
  `discount_amount` INT          NOT NULL,
  `total_amount`    INT          NOT NULL,
  `recipient_name`  VARCHAR(60)  NOT NULL,
  `phone`           VARCHAR(40)  NOT NULL,
  `address1`        VARCHAR(200) NOT NULL,
  `address2`        VARCHAR(200) NULL,
  `zipcode`         VARCHAR(16)  NOT NULL,
  `note`            VARCHAR(1000) NULL,
  `idempotency_key` VARCHAR(64)  NULL,
  `paid_at`         DATETIME NULL,
  `shipped_at`      DATETIME NULL,
  `delivered_at`    DATETIME NULL,
  `canceled_at`     DATETIME NULL,
  `expired_at`      DATETIME NULL,
  `created_at`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uq_orders_order_no` (`order_no`),
  UNIQUE KEY `uq_orders_idemkey` (`idempotency_key`),
  KEY `idx_orders_account_created` (`account_id`, `created_at`),
  KEY `idx_orders_status_created` (`status`, `created_at`),
  CONSTRAINT `fk_orders_user` FOREIGN KEY (`account_id`) REFERENCES `users`(`id`)
  -- CHECK (`status` IN ('CREATED','PAYMENT_PENDING','PAID','PROCESSING','PACKED','SHIPPED','DELIVERED','COMPLETED','CANCELED','EXPIRED','PAYMENT_FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- ORDER ITEM
CREATE TABLE IF NOT EXISTS `order_item` (
  `order_item_id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT NOT NULL,
  `work_id`       BIGINT NOT NULL,
  `option_hash`   VARCHAR(512) NOT NULL,
  `name`          VARCHAR(120) NOT NULL,
  `thumb_url`     VARCHAR(512) NULL,
  `unit_price`    INT NOT NULL,
  `quantity`      INT NOT NULL,
  `line_total`    INT NOT NULL,
  PRIMARY KEY (`order_item_id`),
  KEY `idx_order_item_order` (`order_id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE
);


-- PAYMENT
CREATE TABLE IF NOT EXISTS `payment` (
  `payment_id`    BIGINT NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT NOT NULL,
  `status`        VARCHAR(32) NOT NULL,
  `method`        VARCHAR(32) NULL,
  `amount`        INT NOT NULL,
  `provider`      VARCHAR(32) NULL,
  `provider_tx_id` VARCHAR(64) NULL,
  `raw_callback`  LONGTEXT NULL,
  `approved_at`   DATETIME NULL,
  `created_at`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`payment_id`),
  KEY `idx_payment_order` (`order_id`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE
  -- CHECK (`status` IN ('NOT_REQUIRED','INITIATED','PENDING','AUTHORIZED','PAID','PARTIAL_REFUNDED','REFUND_PENDING','REFUNDED','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- ORDER EVENT
CREATE TABLE IF NOT EXISTS `order_event` (
  `order_event_id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id`       BIGINT NOT NULL,
  `from_status`    VARCHAR(32) NULL,
  `to_status`      VARCHAR(32) NOT NULL,
  `reason`         VARCHAR(200) NULL,
  `meta`           LONGTEXT NULL,
  `created_at`     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_event_id`),
  KEY `idx_order_event_order` (`order_id`, `created_at`),
  CONSTRAINT `fk_order_event_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE
  -- CHECK (`to_status` IN ('CREATED','PAYMENT_PENDING','PAID','PROCESSING','PACKED','SHIPPED','DELIVERED','COMPLETED','CANCELED','EXPIRED','PAYMENT_FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
