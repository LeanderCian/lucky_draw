package com.leander.lottery.lottery.service.impl;

import com.leander.lottery.lottery.entity.LotteryCount;
import com.leander.lottery.lottery.entity.LotteryCountId;
import com.leander.lottery.lottery.repository.CampaignRepository;
import com.leander.lottery.lottery.repository.LotteryCountRepository;
import com.leander.lottery.lottery.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LotteryCountServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private LotteryCountRepository lotteryCountRepository;

    @InjectMocks
    @Spy // 使用 Spy 是為了能部分模擬 Service 內的方法（如 syncItemToRedis）
    private LotteryCountServiceImpl lotteryCountService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(lotteryCountService, "hashTagPrefix", "campaign_");
        ReflectionTestUtils.setField(lotteryCountService, "lotteryCountKeyPrefix", "lottery_count_");
    }

    @Test
    void shouldCreateSuccessfully() {
        // 1. 準備測試數據
        Long campaignId = 123L;
        Long userId = 456L;
        Integer totalLotteryCount = 777;

        // 2. 設定 Mock 行為
        when(campaignRepository.existsById(any(Long.class))).thenReturn(true);
        when(userRepository.existsById(any(Long.class))).thenReturn(true);
        when(lotteryCountRepository.save(any(LotteryCount.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        // 3. 執行測試
        LotteryCount result = lotteryCountService.updateLotteryCount(campaignId, userId, totalLotteryCount);

        // 4. 驗證結果
        assertNotNull(result);
        assertEquals(campaignId, result.getCampaignId());
        assertEquals(userId, result.getUserId());
        assertEquals(totalLotteryCount, result.getTotalLotteryCount());
        assertEquals(totalLotteryCount, result.getRemainingLotteryCount());

        // 5. 驗證互動
        // 確認資料庫有存檔
        verify(lotteryCountRepository, times(1)).save(any(LotteryCount.class));
    }

    @Test
    void shouldUpdateSuccessfully() {
        // 1. 準備測試數據
        Long campaignId = 123L;
        Long userId = 456L;
        Integer totalLotteryCount = 888;

        LotteryCount existedLotteryCount = new LotteryCount();
        existedLotteryCount.setCampaignId(campaignId);
        existedLotteryCount.setUserId(userId);
        existedLotteryCount.setTotalLotteryCount(777);
        existedLotteryCount.setRemainingLotteryCount(555);

        // 2. 設定 Mock 行為
        when(campaignRepository.existsById(any(Long.class))).thenReturn(true);
        when(userRepository.existsById(any(Long.class))).thenReturn(true);
        when(lotteryCountRepository.save(any(LotteryCount.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });
        when(lotteryCountRepository.findById(any(LotteryCountId.class))).thenReturn(Optional.of(existedLotteryCount));

        // 3. 執行測試
        LotteryCount result = lotteryCountService.updateLotteryCount(campaignId, userId, totalLotteryCount);

        // 4. 驗證結果
        assertNotNull(result);
        assertEquals(campaignId, result.getCampaignId());
        assertEquals(userId, result.getUserId());
        assertEquals(totalLotteryCount, result.getTotalLotteryCount());
        assertEquals(666, result.getRemainingLotteryCount());

        // 5. 驗證互動
        // 確認資料庫有存檔
        verify(lotteryCountRepository, times(1)).save(any(LotteryCount.class));
    }
}
