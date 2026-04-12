package com.leander.lottery.lottery.service.impl;

import com.leander.lottery.lottery.dto.CreateItemRequest;
import com.leander.lottery.lottery.dto.UpdateItemRequest;
import com.leander.lottery.lottery.dto.UpdateItemStockRequest;
import com.leander.lottery.lottery.entity.Item;
import com.leander.lottery.lottery.exception.ProbabilityExceededException;
import com.leander.lottery.lottery.exception.StockNotEnoughException;
import com.leander.lottery.lottery.repository.CampaignRepository;
import com.leander.lottery.lottery.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        ReflectionTestUtils.setField(itemService, "hashTagPrefix", "campaign_");
        ReflectionTestUtils.setField(itemService, "campaignItemListKeyPrefix", "campaign_itemlist_");
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

    @Test
    void createItemWithProbabilityFull() {
        // 1. 準備數據
        Long campaignId = 111L;
        CreateItemRequest input = new CreateItemRequest();
        input.setCampaignId(campaignId);
        input.setName("test");
        input.setProbability(10000000);
        input.setTotalStock(10L);

        Item saved = new Item();
        saved.setId(777L);
        saved.setCampaignId(input.getCampaignId());
        saved.setName(input.getName());
        saved.setProbability(input.getProbability());
        saved.setTotalStock(input.getTotalStock());
        saved.setCurrentStock(input.getTotalStock());

        // 模擬資料庫中已存在兩個獎品，加總機率為 90%
        Integer totalProbability = 90000000;

        // 2. 模擬 Repository 行為
        when(campaignRepository.existsById(any(Long.class))).thenReturn(true);
        when(itemRepository.sumProbabilityByCampaignId(campaignId)).thenReturn(totalProbability);
        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        // 3. 執行測試
        Long result = itemService.createItem(input);

        // 4. 驗證結果
        assertNotNull(result);
    }

    @Test
    void createItemWithProbabilityExceeded() {
        // 1. 準備數據
        Long campaignId = 1L;

        // 模擬資料庫中已存在兩個獎品，加總機率為 90%
        Integer totalProbability = 90000000;

        // 2. 模擬 Repository 行為
        when(campaignRepository.existsById(any(Long.class))).thenReturn(true);
        when(itemRepository.sumProbabilityByCampaignId(campaignId)).thenReturn(totalProbability);

        // 3. 準備要新增的獎品
        CreateItemRequest req = new CreateItemRequest();
        req.setName("爆量獎品");
        req.setCampaignId(1L);
        req.setProbability(10000001);
        req.setTotalStock(10L);

        // 4. 執行並驗證
        Exception exception = assertThrows(ProbabilityExceededException.class, () -> {
            itemService.createItem(req);
        });

        // 5. 驗證錯誤訊息
        assertEquals("probability exceeded", exception.getMessage());

        // 6. 驗證防護機制：因為機率爆了，save 方法「不應該」被呼叫
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldUpdateItemSuccessfully() {
        // 1. 準備測試數據
        Long itemId = 123L;
        Long campaignId = 456L;
        UpdateItemRequest input = new UpdateItemRequest();
        input.setName("test");
        input.setProbability(15000000);

        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setCampaignId(campaignId);
        oldItem.setName("old");
        oldItem.setProbability(5000000);
        oldItem.setTotalStock(1500L);
        oldItem.setCurrentStock(500L);

        // 2. 設定 Mock 行為
        when(itemRepository.findById(any(Long.class))).thenReturn(Optional.of(oldItem));

        // 模擬 Repository 儲存物件
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        // 3. 執行測試
        Item result = itemService.updateItem(itemId, input);

        // 4. 驗證結果
        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals(campaignId, result.getCampaignId());
        assertEquals(input.getName(), result.getName());
        assertEquals(input.getProbability(), result.getProbability());
        assertEquals(oldItem.getTotalStock(), result.getTotalStock());
        assertEquals(oldItem.getCurrentStock(), result.getCurrentStock());

        // 5. 驗證互動
        // 確認資料庫有存檔
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItemWithProbabilityFull() {
        // 1. 準備測試數據
        Long itemId = 123L;
        Long campaignId = 456L;
        UpdateItemRequest input = new UpdateItemRequest();
        input.setName("test");
        input.setProbability(55000000);

        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setCampaignId(campaignId);
        oldItem.setName("old");
        oldItem.setProbability(5000000);
        oldItem.setTotalStock(1500L);
        oldItem.setCurrentStock(500L);

        // 模擬資料庫中已存在兩個獎品，加總機率為 50%
        Integer totalProbability = 50000000;

        // 2. 模擬 Repository 行為
        // 2. 設定 Mock 行為
        when(itemRepository.findById(any(Long.class))).thenReturn(Optional.of(oldItem));
        when(itemRepository.sumProbabilityByCampaignId(campaignId)).thenReturn(totalProbability);
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        // 3. 執行測試
        Item result = itemService.updateItem(itemId, input);

        // 4. 驗證結果
        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals(campaignId, result.getCampaignId());
        assertEquals(input.getName(), result.getName());
        assertEquals(input.getProbability(), result.getProbability());
        assertEquals(oldItem.getTotalStock(), result.getTotalStock());
        assertEquals(oldItem.getCurrentStock(), result.getCurrentStock());

        // 5. 驗證互動
        // 確認資料庫有存檔
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItemWithProbabilityExceeded() {
        // 1. 準備測試數據
        Long itemId = 123L;
        Long campaignId = 456L;
        UpdateItemRequest input = new UpdateItemRequest();
        input.setName("test");
        input.setProbability(55000000);

        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setCampaignId(campaignId);
        oldItem.setName("old");
        oldItem.setProbability(5000000);
        oldItem.setTotalStock(1500L);
        oldItem.setCurrentStock(500L);

        // 模擬資料庫中已存在兩個獎品，加總機率為 90%
        Integer totalProbability = 90000000;

        // 2. 模擬 Repository 行為
        when(itemRepository.findById(any(Long.class))).thenReturn(Optional.of(oldItem));
        when(itemRepository.sumProbabilityByCampaignId(campaignId)).thenReturn(totalProbability);

        // 3. 執行測試
        Exception exception = assertThrows(ProbabilityExceededException.class, () -> {
            itemService.updateItem(itemId, input);
        });

        // 4. 驗證錯誤訊息
        assertEquals("probability exceeded", exception.getMessage());

        // 5. 驗證防護機制：因為機率爆了，save 方法「不應該」被呼叫
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldUpdateItemStockSuccessfully() {
        // 1. 準備測試數據
        Long itemId = 123L;
        Long campaignId = 456L;
        UpdateItemStockRequest input = new UpdateItemStockRequest();
        input.setIncrementAmount(111L);

        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setCampaignId(campaignId);
        oldItem.setName("item1");
        oldItem.setProbability(5000000);
        oldItem.setTotalStock(1500L);
        oldItem.setCurrentStock(500L);

        // 2. 設定 Mock 行為
        when(itemRepository.findById(any(Long.class))).thenReturn(Optional.of(oldItem));

        // 模擬 Repository 儲存物件
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        // 模擬 Redis 的操作類別 (因為 RedisTemplate 是鏈式呼叫，需要 Mock 內層操作)
        when(redisTemplate.execute(
                any(RedisScript.class), // 匹配任何 Redis 腳本
                anyList(),              // 匹配任何 KEYS 列表
                any(),                  // 匹配任何可變參數 (ARGV1)
                any()                   // 匹配任何可變參數 (ARGV2)
        )).thenReturn(611L);


        // 3. 執行測試
        Item result = itemService.updateItemStock(itemId, input.getIncrementAmount());

        // 4. 驗證結果
        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals(campaignId, result.getCampaignId());
        assertEquals(oldItem.getName(), result.getName());
        assertEquals(oldItem.getProbability(), result.getProbability());
        assertEquals(1611L, result.getTotalStock());
        assertEquals(611L, result.getCurrentStock());

        // 5. 驗證互動
        // 確認資料庫有存檔
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItemWithStockBeZero() {
        // 1. 準備測試數據
        Long itemId = 123L;
        Long campaignId = 456L;
        UpdateItemStockRequest input = new UpdateItemStockRequest();
        input.setIncrementAmount(-500L);

        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setCampaignId(campaignId);
        oldItem.setName("item1");
        oldItem.setProbability(5000000);
        oldItem.setTotalStock(1500L);
        oldItem.setCurrentStock(500L);

        // 2. 設定 Mock 行為
        when(itemRepository.findById(any(Long.class))).thenReturn(Optional.of(oldItem));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        // 模擬 Redis 的操作類別 (因為 RedisTemplate 是鏈式呼叫，需要 Mock 內層操作)
        when(redisTemplate.execute(
                any(RedisScript.class), // 匹配任何 Redis 腳本
                anyList(),              // 匹配任何 KEYS 列表
                any(),                  // 匹配任何可變參數 (ARGV1)
                any()                   // 匹配任何可變參數 (ARGV2)
        )).thenReturn(-0L);

        // 3. 執行測試
        Item result = itemService.updateItemStock(itemId, input.getIncrementAmount());

        // 4. 驗證結果
        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals(campaignId, result.getCampaignId());
        assertEquals(oldItem.getName(), result.getName());
        assertEquals(oldItem.getProbability(), result.getProbability());
        assertEquals(1000L, result.getTotalStock());
        assertEquals(0L, result.getCurrentStock());

        // 5. 驗證互動
        // 確認資料庫有存檔
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItemWithStockNotEnough() {
        // 1. 準備測試數據
        Long itemId = 123L;
        Long campaignId = 456L;
        UpdateItemStockRequest input = new UpdateItemStockRequest();
        input.setIncrementAmount(-600L);

        Item oldItem = new Item();
        oldItem.setId(itemId);
        oldItem.setCampaignId(campaignId);
        oldItem.setName("item1");
        oldItem.setProbability(5000000);
        oldItem.setTotalStock(1500L);
        oldItem.setCurrentStock(500L);

        // // 2. 設定 Mock 行為
        when(itemRepository.findById(any(Long.class))).thenReturn(Optional.of(oldItem));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgument(0);
                });

        // 模擬 Redis 的操作類別 (因為 RedisTemplate 是鏈式呼叫，需要 Mock 內層操作)
        when(redisTemplate.execute(
                any(RedisScript.class), // 匹配任何 Redis 腳本
                anyList(),              // 匹配任何 KEYS 列表
                any(),                  // 匹配任何可變參數 (ARGV1)
                any()                   // 匹配任何可變參數 (ARGV2)
        )).thenReturn(-1L);


        // 3. 執行測試
        Exception exception = assertThrows(StockNotEnoughException.class, () -> {
            itemService.updateItemStock(itemId, input.getIncrementAmount());
        });

        // 4. 驗證錯誤訊息
        assertEquals("item stock is not enough", exception.getMessage());
    }
}
