package com.leander.lottery.admin.service.impl;

import com.leander.lottery.admin.dto.DrawRequest;
import com.leander.lottery.admin.dto.DrawResponse;
import com.leander.lottery.admin.entity.Campaign;
import com.leander.lottery.admin.exception.ExceedMaxTriesException;
import com.leander.lottery.admin.exception.RemainingCountNotEnoughException;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LotteryServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    @Spy // 使用 Spy 是為了能部分模擬 Service 內的方法
    private LotteryServiceImpl lotteryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(lotteryService, "hashTagPrefix", "campaign_");
        ReflectionTestUtils.setField(lotteryService, "campaignItemListKeyPrefix", "campaign_itemlist_");
        ReflectionTestUtils.setField(lotteryService, "campaignKeyPrefix", "campaign_");
        ReflectionTestUtils.setField(lotteryService, "lotteryCountKeyPrefix", "lottery_count_");

        ReflectionTestUtils.setField(lotteryService, "objectMapper", objectMapper);
    }

    @Test
    void shouldDrawSuccessfully() {
        // 1. 準備測試數據
        Long userId = 222L;
        DrawRequest req = new DrawRequest();
        req.setCampaignId(111L);
        req.setCount(3);

        Campaign campaign = new Campaign();
        campaign.setId(111L);
        campaign.setName("campaign");
        campaign.setMaxTries(20);
        campaign.setStartTime(0L);
        campaign.setEndTime(System.currentTimeMillis() + 86400000);

        DrawResponse.DrawResult result1 = new DrawResponse.DrawResult();
        result1.setDrawId("1");
        result1.setItemId(1L);
        result1.setItemName("item1");
        result1.setWin(true);

        DrawResponse.DrawResult result2 = new DrawResponse.DrawResult();
        result2.setDrawId("2");
        result2.setItemId(2L);
        result2.setItemName("item2");
        result2.setWin(true);

        DrawResponse.DrawResult result3 = new DrawResponse.DrawResult();
        result3.setDrawId("3");
        result3.setItemName("銘謝惠顧");
        result3.setWin(false);

        ArrayList<DrawResponse.DrawResult> drawResults = new ArrayList<>();
        drawResults.add(result1);
        drawResults.add(result2);
        drawResults.add(result3);

        // 2. 設定 Mock 行為
        // 模擬 redis 行為

        // 模擬從 Redis 取得 Campaign 字串
        String campaignJson = objectMapper.writeValueAsString(campaign);
        when(valueOperations.get(anyString())).thenReturn(campaignJson);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.execute(
                any(),
                anyList(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(objectMapper.writeValueAsString(drawResults));

        // 3. 執行測試
        DrawResponse result = lotteryService.executeDraw(userId, req);

        // 4. 驗證結果
        assertNotNull(result);
        assertEquals(3, result.getResults().size());
        assertEquals(drawResults.get(0).getItemId(), result.getResults().get(0).getItemId());
        assertEquals(drawResults.get(0).getItemName(), result.getResults().get(0).getItemName());
        assertEquals(drawResults.get(0).isWin(), result.getResults().get(0).isWin());
        assertEquals(drawResults.get(1).getItemId(), result.getResults().get(1).getItemId());
        assertEquals(drawResults.get(1).getItemName(), result.getResults().get(1).getItemName());
        assertEquals(drawResults.get(1).isWin(), result.getResults().get(1).isWin());
        assertEquals(drawResults.get(2).getItemId(), result.getResults().get(2).getItemId());
        assertEquals(drawResults.get(2).getItemName(), result.getResults().get(2).getItemName());
        assertEquals(drawResults.get(2).isWin(), result.getResults().get(2).isWin());
    }

    @Test
    void campaignNotFound() {
        // 1. 準備測試數據
        Long userId = 222L;
        DrawRequest req = new DrawRequest();
        req.setCampaignId(111L);
        req.setCount(3);

        // 2. 設定 Mock 行為
        // 模擬 redis 行為

        // 模擬從 Redis 取得 Campaign 字串
        when(valueOperations.get(anyString())).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 3. 執行並驗證
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            lotteryService.executeDraw(userId, req);
        });

        // 4. 驗證錯誤訊息
        assertEquals("No this campaign", exception.getMessage());

        // 5. 驗證防護機制
        verify(lotteryService, never()).recordDrawResult(any());
    }

    @Test
    void campaignHasEnded() {
        // 1. 準備測試數據
        Long userId = 222L;
        DrawRequest req = new DrawRequest();
        req.setCampaignId(111L);
        req.setCount(3);

        Campaign campaign = new Campaign();
        campaign.setId(111L);
        campaign.setName("campaign");
        campaign.setMaxTries(20);
        campaign.setStartTime(0L);
        campaign.setEndTime(System.currentTimeMillis() - 86400000);

        // 2. 設定 Mock 行為
        // 模擬 redis 行為

        // 模擬從 Redis 取得 Campaign 字串
        String campaignJson = objectMapper.writeValueAsString(campaign);
        when(valueOperations.get(anyString())).thenReturn(campaignJson);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 3. 執行並驗證
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            lotteryService.executeDraw(userId, req);
        });

        // 4. 驗證錯誤訊息
        assertEquals("Campaign has ended", exception.getMessage());

        // 5. 驗證防護機制
        verify(lotteryService, never()).recordDrawResult(any());
    }

    @Test
    void exceedMaxTries() {
        // 1. 準備測試數據
        Long userId = 222L;
        DrawRequest req = new DrawRequest();
        req.setCampaignId(111L);
        req.setCount(30);

        Campaign campaign = new Campaign();
        campaign.setId(111L);
        campaign.setName("campaign");
        campaign.setMaxTries(20);
        campaign.setStartTime(0L);
        campaign.setEndTime(System.currentTimeMillis() + 86400000);

        // 2. 設定 Mock 行為
        // 模擬 redis 行為

        // 模擬從 Redis 取得 Campaign 字串
        String campaignJson = objectMapper.writeValueAsString(campaign);
        when(valueOperations.get(anyString())).thenReturn(campaignJson);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 3. 執行並驗證
        Exception exception = assertThrows(ExceedMaxTriesException.class, () -> {
            lotteryService.executeDraw(userId, req);
        });

        // 4. 驗證錯誤訊息
        assertEquals("Exceed max tries", exception.getMessage());

        // 5. 驗證防護機制
        verify(lotteryService, never()).recordDrawResult(any());
    }

    @Test
    void remainingCountNotEnough() {
        // 1. 準備測試數據
        Long userId = 222L;
        DrawRequest req = new DrawRequest();
        req.setCampaignId(111L);
        req.setCount(3);

        Campaign campaign = new Campaign();
        campaign.setId(111L);
        campaign.setName("campaign");
        campaign.setMaxTries(20);
        campaign.setStartTime(0L);
        campaign.setEndTime(System.currentTimeMillis() + 86400000);

        ArrayList<DrawResponse.DrawResult> drawResults = new ArrayList<>();

        // 2. 設定 Mock 行為
        // 模擬 redis 行為

        // 模擬從 Redis 取得 Campaign 字串
        String campaignJson = objectMapper.writeValueAsString(campaign);
        when(valueOperations.get(anyString())).thenReturn(campaignJson);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.execute(
                any(),
                anyList(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(objectMapper.writeValueAsString(drawResults));

        // 3. 執行並驗證
        Exception exception = assertThrows(RemainingCountNotEnoughException.class, () -> {
            lotteryService.executeDraw(userId, req);
        });

        // 4. 驗證錯誤訊息
        assertEquals("Remaining count is not enough", exception.getMessage());

        // 5. 驗證防護機制
        verify(lotteryService, never()).recordDrawResult(any());
    }
}
