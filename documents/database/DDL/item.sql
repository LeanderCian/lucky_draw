CREATE TABLE `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '唯一識別碼',
    `campaign_id` BIGINT NOT NULL COMMENT '所屬活動 ID',
    `name` VARCHAR(255) NOT NULL COMMENT '獎品名稱',
    `probability` INT NOT NULL COMMENT '中獎機率，單位微(micro)，例如 1,000,000 代表 1%',
    `total_stock` BIGINT NOT NULL DEFAULT 0 COMMENT '總庫存量',
    `current_stock` BIGINT NOT NULL DEFAULT 0 COMMENT '目前剩餘庫存',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_item_campaign` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`) ON DELETE CASCADE,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;