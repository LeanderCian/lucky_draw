CREATE TABLE `lottery_count` (
    `campaign_id` BIGINT NOT NULL COMMENT '活動 ID',
    `user_id` BIGINT NOT NULL COMMENT '使用者 ID',
    `total_lottery_count` INT NOT NULL DEFAULT 0 COMMENT '該使用者獲得的總抽獎機會次數',
    `remaining_lottery_count` INT NOT NULL DEFAULT 0 COMMENT '該使用者剩餘的抽獎次數',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`campaign_id`, `user_id`),
    CONSTRAINT `fk_mapping_campaign` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_mapping_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;