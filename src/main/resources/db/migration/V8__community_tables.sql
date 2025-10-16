-- Community feature tables: post, like, comment, copy
-- Based on the recommended design (works linked via FK, with snapshot fields)

-- COMMUNITY POST
CREATE TABLE IF NOT EXISTS `community_post` (
  `post_id`           BIGINT NOT NULL AUTO_INCREMENT,
  `work_id`           BIGINT NOT NULL,
  `author_id`         BIGINT NOT NULL,
  `title`             VARCHAR(200) NULL,
  `description`       TEXT NULL,
  `visibility`        VARCHAR(16) NOT NULL DEFAULT 'PUBLIC',  -- PUBLIC | UNLISTED | PRIVATE
  `status`            VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | REMOVED | HIDDEN | MOD_PENDING
  `allow_copy`        TINYINT(1) NOT NULL DEFAULT 0,
  `allow_comments`    TINYINT(1) NOT NULL DEFAULT 1,
  `published_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `like_count`        INT NOT NULL DEFAULT 0,
  `comment_count`     INT NOT NULL DEFAULT 0,
  -- Snapshot for stable rendering/copy
  `snapshot_name`       VARCHAR(200) NULL,
  `snapshot_thumb_url`  VARCHAR(512) NULL,
  `snapshot_payload`    JSON NULL,
  `created_at`        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`),
  KEY `idx_post_visibility_published` (`visibility`, `published_at`),
  KEY `idx_post_author` (`author_id`, `created_at`),
  KEY `idx_post_work` (`work_id`),
  CONSTRAINT `fk_post_work` FOREIGN KEY (`work_id`) REFERENCES `works`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_post_author` FOREIGN KEY (`author_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
  -- CHECK (`visibility` IN ('PUBLIC','UNLISTED','PRIVATE')),
  -- CHECK (`status` IN ('ACTIVE','REMOVED','HIDDEN','MOD_PENDING'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- COMMUNITY LIKE
CREATE TABLE IF NOT EXISTS `community_like` (
  `post_id`    BIGINT NOT NULL,
  `user_id`    BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uq_like` (`post_id`, `user_id`),
  KEY `idx_like_user` (`user_id`, `created_at`),
  CONSTRAINT `fk_like_post` FOREIGN KEY (`post_id`) REFERENCES `community_post`(`post_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- COMMUNITY COMMENT (supports replies via parent_id)
CREATE TABLE IF NOT EXISTS `community_comment` (
  `comment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id`    BIGINT NOT NULL,
  `user_id`    BIGINT NOT NULL,
  `content`    TEXT NOT NULL,
  `parent_id`  BIGINT NULL,
  `status`     VARCHAR(16) NOT NULL DEFAULT 'VISIBLE',  -- VISIBLE | DELETED | HIDDEN | MOD_PENDING
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `idx_comment_post` (`post_id`, `created_at`),
  KEY `idx_comment_parent` (`parent_id`),
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `community_post`(`post_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `community_comment`(`comment_id`) ON DELETE SET NULL
  -- CHECK (`status` IN ('VISIBLE','DELETED','HIDDEN','MOD_PENDING'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- COMMUNITY COPY (record of copy action and mapping to new work)
CREATE TABLE IF NOT EXISTS `community_copy` (
  `copy_id`       BIGINT NOT NULL AUTO_INCREMENT,
  `post_id`       BIGINT NOT NULL,
  `source_work_id` BIGINT NOT NULL,
  `dest_work_id`   BIGINT NOT NULL,
  `copier_id`     BIGINT NOT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`copy_id`),
  UNIQUE KEY `uq_copy_once` (`post_id`, `copier_id`),
  KEY `idx_copy_copier` (`copier_id`, `created_at`),
  CONSTRAINT `fk_copy_post` FOREIGN KEY (`post_id`) REFERENCES `community_post`(`post_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_copy_source_work` FOREIGN KEY (`source_work_id`) REFERENCES `works`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_copy_dest_work` FOREIGN KEY (`dest_work_id`) REFERENCES `works`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_copy_copier` FOREIGN KEY (`copier_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- End of V7__community_tables.sql