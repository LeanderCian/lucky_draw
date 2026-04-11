package com.leander.lottery.admin.service.impl;

import com.leander.lottery.admin.dto.LotteryCountResponse;
import com.leander.lottery.admin.entity.LotteryCount;
import com.leander.lottery.admin.entity.LotteryCountId;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.exception.StockNotEnoughException;
import com.leander.lottery.admin.repository.CampaignRepository;
import com.leander.lottery.admin.repository.LotteryCountRepository;
import com.leander.lottery.admin.repository.UserRepository;
import com.leander.lottery.admin.service.LotteryCountService;
import com.leander.lottery.admin.util.LuaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional(rollbackFor = Exception.class)
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
        Integer incrementAmount = totalLotteryCount - lotteryCount.getTotalLotteryCount();
        Integer remainingLotteryCount = incrementAmount + lotteryCount.getRemainingLotteryCount();
        lotteryCount.setTotalLotteryCount(totalLotteryCount);
        lotteryCount.setRemainingLotteryCount(remainingLotteryCount);

        // 更新 MySQL (建立或修改)
        LotteryCount saved = lotteryCountRepository.save(lotteryCount);

        // 同步至 Redis
        syncLotteryCountToRedis(saved, incrementAmount);

        return saved;
    }

    private void syncLotteryCountToRedis(LotteryCount lotteryCount, Integer incrementAmount) {
        String lotteryCountKey = lotteryCountKeyPrefix + lotteryCount.getCampaignId() + "_" + lotteryCount.getUserId();
        List<String> keys = new ArrayList<>();
        keys.add(lotteryCountKey);

        // 準備腳本執行器
        DefaultRedisScript<Boolean> script = new DefaultRedisScript<>(LuaUtils.SYNC_LOTTERY_COUNT_TO_REDIS_LUA, Boolean.class);

        // 執行腳本
        try {
            redisTemplate.execute(
                    script,
                    keys,
                    incrementAmount
            );
        } catch (StockNotEnoughException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("insert item to Redis failed", e);
        }
    }

    @Transactional(readOnly = true)
    public LotteryCountResponse getLotteryCount(Long campaignId) {
        // 檢查活動是否存在
        if (!campaignRepository.existsById(campaignId)) {
            throw new ResourceNotFoundException("No this campaign");
        }

        // 查詢該活動下所有使用者的抽獎次數
        ArrayList<LotteryCount> lotteryCounts = lotteryCountRepository.findByCampaignId(campaignId);

        // 轉換為 DTO 格式
        List<LotteryCountResponse.UserCount> userCounts = lotteryCounts.stream()
                .map(c -> new LotteryCountResponse.UserCount(
                        c.getUserId(),
                        c.getTotalLotteryCount(),
                        c.getRemainingLotteryCount()
                ))
                .collect(Collectors.toList());

        return new LotteryCountResponse(campaignId, userCounts);
    }
}
