CREATE TABLE `lottery_result` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL COMMENT '使用者ID',
    `campaign_id` bigint(20) NOT NULL COMMENT '活動ID',
    `item_id` bigint(20) DEFAULT NULL COMMENT '中獎獎品ID, 銘謝惠顧則為NULL',
    `no_item` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否為銘謝惠顧: 1-是, 0-否',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_lr_user_idx` (`user_id`),
    KEY `fk_lr_campaign_idx` (`campaign_id`),
    KEY `fk_lr_item_idx` (`item_id`),
    CONSTRAINT `fk_lr_campaign` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`),
    CONSTRAINT `fk_lr_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`),
    CONSTRAINT `fk_lr_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;