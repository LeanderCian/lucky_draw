package com.leander.lottery.admin.service;

import com.leander.lottery.admin.dto.LotteryCountResponse;
import com.leander.lottery.admin.entity.LotteryCount;

public interface LotteryCountService {
    public LotteryCount updateLotteryCount(Long campaignId, Long userId, Integer totalLotteryCount);

    public LotteryCountResponse getLotteryCount(Long campaignId);
}
