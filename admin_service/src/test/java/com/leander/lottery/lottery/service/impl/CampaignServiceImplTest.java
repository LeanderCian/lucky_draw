package com.leander.lottery.lottery.service.impl;

import com.leander.lottery.lottery.dto.CreateCampaignRequest;
import com.leander.lottery.lottery.dto.UpdateCampaignRequest;
import com.leander.lottery.lottery.entity.Campaign;
import com.leander.lottery.lottery.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    @Spy // 使用 Spy 是為了能部分模擬 Service 內的方法（如 syncItemToRedis）
    private CampaignServiceImpl campaignService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(campaignService, "hashTagPrefix", "campaign_");
        ReflectionTestUtils.setField(campaignService, "campaignKeyPrefix", "campaign_");
    }

    @Test
    void shouldCreateCampaignSuccessfully() {
        // 1. 準備測試數據
        CreateCampaignRequest input = new CreateCampaignRequest();
        input.setName("test");
        input.setMaxTries(50);
        input.setStartTime(1000L);
        input.setEndTime(2000L);

        Campaign saved = new Campaign();
        saved.setId(777L);
        saved.setName(input.getName());
        saved.setMaxTries(input.getMaxTries());
        saved.setStartTime(input.getStartTime());
        saved.setEndTime(input.getEndTime());

        // 2. 設定 Mock 行為
        // 模擬 Repository 儲存後回傳帶有 ID 的物件
        when(campaignRepository.save(any(Campaign.class))).thenReturn(saved);

        // 模擬 Redis 的操作類別 (因為 RedisTemplate 是鏈式呼叫，需要 Mock 內層操作)
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 3. 執行測試
        Long result = campaignService.createCampaign(input);

        // 4. 驗證結果
        assertNotNull(result);

        // 5. 驗證互動
        // 確認資料庫有存檔
        verify(campaignRepository, times(1)).save(any(Campaign.class));

        // 確認 Redis 庫存有寫入 (opsForValue().set)
        verify(valueOperations, times(1)).set(
                eq("{campaign_777}_campaign_777"),
                any(Campaign.class)
        );
    }

    @Test
    void shouldUpdateCampaignSuccessfully() {
        // 1. 準備測試數據
        Long campaignId = 777L;

        UpdateCampaignRequest input = new UpdateCampaignRequest();
        input.setName("test");
        input.setMaxTries(50);
        input.setStartTime(1000L);
        input.setEndTime(2000L);

        Campaign oldCampaign = new Campaign();
        oldCampaign.setId(campaignId);
        oldCampaign.setName("old");
        oldCampaign.setMaxTries(30);
        oldCampaign.setStartTime(500L);
        oldCampaign.setEndTime(1500L);

        // 2. 設定 Mock 行為
        when(campaignRepository.findById(any(Long.class))).thenReturn(Optional.of(oldCampaign));

        // 模擬 Repository 儲存物件
        when(campaignRepository.save(any(Campaign.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        // 模擬 Redis 的操作類別 (因為 RedisTemplate 是鏈式呼叫，需要 Mock 內層操作)
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 3. 執行測試
        Campaign result = campaignService.updateCampaign(campaignId, input);

        // 4. 驗證結果
        assertNotNull(result);
        assertEquals(campaignId, result.getId());
        assertEquals(input.getName(), result.getName());
        assertEquals(input.getMaxTries(), result.getMaxTries());
        assertEquals(input.getStartTime(), result.getStartTime());
        assertEquals(input.getEndTime(), result.getEndTime());

        // 5. 驗證互動
        // 確認資料庫有存檔
        verify(campaignRepository, times(1)).save(any(Campaign.class));

        // 確認 Redis 庫存有寫入 (opsForValue().set)
        verify(valueOperations, times(1)).set(
                eq("{campaign_777}_campaign_777"),
                any(Campaign.class)
        );
    }
}
