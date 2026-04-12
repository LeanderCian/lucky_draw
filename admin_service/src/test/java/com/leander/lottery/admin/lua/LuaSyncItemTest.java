package com.leander.lottery.admin.lua;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leander.lottery.admin.entity.Item;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.embedded.RedisServer;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LuaSyncItemTest {

    private static RedisServer redisServer;
    private RedisTemplate<String, Object> redisTemplate;
    private DefaultRedisScript<Long> script;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void startRedis() {
        // 啟動內嵌 Redis 伺服器
        redisServer = new RedisServer(7379);
        redisServer.start();
    }

    @AfterAll
    static void stopRedis() {
        redisServer.stop();
    }

    @BeforeEach
    void setUp() {
        // 設定 Redis 連線
        LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", 7379);
        factory.afterPropertiesSet();
        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.afterPropertiesSet();

        // 清空資料
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 載入 Lua 腳本
        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/sync_item.lua"));
        script.setResultType(Long.class);
    }

    @Test
    void testSyncItem() throws Exception {
        // 驗證 create item
        // 1. 準備資料
        Long campaignId = 111L;
        Item item = new Item();
        item.setId(333L);
        item.setCampaignId(campaignId);
        item.setName("item333");
        item.setProbability(20000000);
        item.setTotalStock(1000L);
        item.setCurrentStock(500L);

        String campaignItemListKey = "{campaign_" + campaignId + "}_campaign_itemlist_" + campaignId;
        List<String> keys = Arrays.asList(campaignItemListKey, item.getId().toString());

        // 3. 執行 Lua
        redisTemplate.execute(script, keys, item);

        // 4. 驗證
        // 從 Redis 抓回資料驗證
        Item result = (Item) redisTemplate.opsForHash().get(campaignItemListKey, item.getId().toString());
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getCampaignId(), result.getCampaignId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getProbability(), result.getProbability());
        assertEquals(item.getTotalStock(), result.getTotalStock());
        assertEquals(item.getCurrentStock(), result.getCurrentStock());

        // 驗證 update item
        // 1. 準備資料
        Item newItem = new Item();
        newItem.setId(333L);
        newItem.setCampaignId(campaignId);
        newItem.setName("item333_new");
        newItem.setProbability(30000000);
        newItem.setTotalStock(2000L);
        newItem.setCurrentStock(1500L);

        // 3. 執行 Lua
        redisTemplate.execute(script, keys, newItem);

        // 4. 驗證
        // 從 Redis 抓回資料驗證
        Item newResult = (Item) redisTemplate.opsForHash().get(campaignItemListKey, item.getId().toString());
        assertEquals(newItem.getId(), newResult.getId());
        assertEquals(newItem.getCampaignId(), newResult.getCampaignId());
        assertEquals(newItem.getName(), newResult.getName());
        assertEquals(newItem.getProbability(), newResult.getProbability());
        assertEquals(newItem.getTotalStock(), newResult.getTotalStock());
        assertEquals(newItem.getCurrentStock(), newResult.getCurrentStock());
    }
}