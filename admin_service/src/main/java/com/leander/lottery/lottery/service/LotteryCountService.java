package com.leander.lottery.lottery.service;

import com.leander.lottery.lottery.dto.LotteryCountResponse;
import com.leander.lottery.lottery.entity.LotteryCount;

public interface LotteryCountService {
    public LotteryCount updateLotteryCount(Long campaignId, Long userId, Integer totalLotteryCount);

    public LotteryCountResponse getLotteryCount(Long campaignId);
}
