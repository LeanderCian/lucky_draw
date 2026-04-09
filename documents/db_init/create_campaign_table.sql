CREATE TABLE IF NOT EXISTS `campaign` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '唯一識別碼',
    `name` VARCHAR(255) NOT NULL COMMENT '活動名稱',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0: inactive, 1: active',
    `max_tries` INT NOT NULL DEFAULT 1 COMMENT '單一用戶最大抽獎次數上限',
    `start_time` BIGINT NOT NULL COMMENT '活動開始時間',
    `end_time` BIGINT NOT NULL COMMENT '活動結束時間',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;