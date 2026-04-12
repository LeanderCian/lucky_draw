package com.leander.lottery.admin.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor          // 1. 必須有預設建構子
@AllArgsConstructor         // 2. 方便手動 new
@EqualsAndHashCode        // 3. 複合主鍵必須實作 equals 和 hashCode
public class LotteryCountId implements Serializable {
    private Long campaignId;
    private Long userId;
}
