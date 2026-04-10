package com.leander.lottery.admin.service.impl;

import com.leander.lottery.admin.dto.CreateItemRequest;
import com.leander.lottery.admin.entity.Campaign;
import com.leander.lottery.admin.entity.Item;
import com.leander.lottery.admin.repository.CampaignRepository;
import com.leander.lottery.admin.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    @Spy // 使用 Spy 是為了能部分模擬 Service 內的方法（如 syncItemToRedis）
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(itemService, "campaignItemListKeyPrefix", "campaign_itemlist_");
        ReflectionTestUtils.setField(itemService, "itemStockKeyPrefix", "item_stock_");
    }

    @Test
    void shouldCreateItemSuccessfully() {
        // 1. 準備測試數據
        CreateItemRequest input = new CreateItemRequest();
        input.setCampaignId(111L);
        input.setName("test");
        input.setProbability(15000000);
        input.setTotalStock(10L);

        Item saved = new Item();
        saved.setId(777L);
        saved.setCampaignId(input.getCampaignId());
        saved.setName(input.getName());
        saved.setProbability(input.getProbability());
        saved.setTotalStock(input.getTotalStock());
        saved.setCurrentStock(input.getTotalStock());

        // 2. 設定 Mock 行為
        // 模擬 CampaignRepository找到該活動
        when(campaignRepository.existsById(any(Long.class))).thenReturn(true);

        // 模擬 ItemRepository 儲存後回傳帶有 ID 的物件
        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        // 3. 執行測試
        Long result = itemService.createItem(input);

        // 4. 驗證結果
        assertNotNull(result);
    }
}
