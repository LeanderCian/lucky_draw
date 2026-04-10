package com.leander.lottery.admin.service.impl;

import com.leander.lottery.admin.entity.LotteryCount;
import com.leander.lottery.admin.entity.LotteryCountId;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.repository.CampaignRepository;
import com.leander.lottery.admin.repository.LotteryCountRepository;
import com.leander.lottery.admin.repository.UserRepository;
import com.leander.lottery.admin.service.LotteryCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LotteryCountServiceImpl implements LotteryCountService {

    @Value("${redis.key.prefix.lottery.count}")
    private String lotteryCountKeyPrefix;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LotteryCountRepository lotteryCountRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public LotteryCount updateLotteryCount(Long campaignId, Long userId, Integer totalLotteryCount) {
        // 檢查活動是否存在
        if (!campaignRepository.existsById(campaignId)) {
            throw new ResourceNotFoundException("No this campaign");
        }

        // 檢查使用者是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("No this user");
        }

        // 確認是否存在
        LotteryCountId lotteryCountId = new LotteryCountId(campaignId, userId);
        LotteryCount lotteryCount = lotteryCountRepository.findById(lotteryCountId)
                .orElse(new LotteryCount(campaignId, userId));

        // 更新總抽獎次數與剩餘抽獎次數
        Integer remainingLotteryCount = totalLotteryCount - lotteryCount.getTotalLotteryCount() + lotteryCount.getRemainingLotteryCount();
        lotteryCount.setTotalLotteryCount(totalLotteryCount);
        lotteryCount.setRemainingLotteryCount(remainingLotteryCount);

        // 更新 MySQL (建立或修改)
        LotteryCount saved = lotteryCountRepository.save(lotteryCount);

        // 同步至 Redis (Lua 確保原子性)
        // 同步至 Redis
        syncLotteryCountToRedis(saved);

        return saved;
    }

    private void syncLotteryCountToRedis(LotteryCount lotteryCount) {
        String lotteryCountKey = lotteryCountKeyPrefix + lotteryCount.getCampaignId() + "_" + lotteryCount.getUserId();
        redisTemplate.opsForValue().set(lotteryCountKey, lotteryCount.getRemainingLotteryCount());
    }
}
