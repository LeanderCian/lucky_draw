package com.leander.lottery.admin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@IdClass(LotteryCountId.class)
@Table(name = "lottery_count")
public class LotteryCount {
    @Id
    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_lottery_count", nullable = false)
    private Integer totalLotteryCount;

    @Column(name = "remaining_lottery_count", nullable = false)
    private Integer remainingLotteryCount;

    @JsonIgnore
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LotteryCount() {}

    public LotteryCount(Long campaignId, Long userId) {
        this.campaignId = campaignId;
        this.userId = userId;
        this.totalLotteryCount = 0;
        this.remainingLotteryCount = 0;
    }

    // 在持久化前自動設定時間
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

