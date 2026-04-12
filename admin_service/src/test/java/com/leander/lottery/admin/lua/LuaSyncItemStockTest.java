package com.leander.lottery.admin.lua;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leander.lottery.admin.entity.Item;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.embedded.RedisServer;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LuaSyncItemStockTest {

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
    void testSyncItemStock() throws Exception {
        // 1. 準備基礎資料並先存入一個獎品
        Long campaignId = 111L;
        String listKey = "{campaign_" + campaignId + "}+_campaign_itemlist_" + campaignId;
        String fieldKey = "333";

        Item initialItem = new Item();
        initialItem.setId(333L);
        initialItem.setName("測試獎品");
        initialItem.setTotalStock(100L);
        initialItem.setCurrentStock(50L); // 初始庫存 50

        // 先存入 Redis
        redisTemplate.opsForHash().put(listKey, fieldKey, initialItem);

        // 準備執行 Lua 的 Script 對象
        DefaultRedisScript<Long> stockScript = new DefaultRedisScript<>();
        stockScript.setLocation(new ClassPathResource("lua/sync_item_stock.lua"));
        stockScript.setResultType(Long.class);

        // 情境 A：正常增加庫存 (+10)
        // ARGV[1]: newTotalStock, ARGV[2]: incrementAmount
        Long resultA = redisTemplate.execute(stockScript, Arrays.asList(listKey, fieldKey), 110L, 10L);
        assertEquals(60L, resultA, "庫存應從 50 變為 60");

        Item itemA = (Item) redisTemplate.opsForHash().get(listKey, fieldKey);
        assertNotNull(itemA);
        assertEquals(60L, itemA.getCurrentStock());
        assertEquals(110L, itemA.getTotalStock());

        // 情境 B：正常減少庫存 (-30)
        Long resultB = redisTemplate.execute(stockScript, Arrays.asList(listKey, fieldKey), 80L, -30L);
        assertEquals(30L, resultB, "庫存應從 60 變為 30");

        Item itemB = (Item) redisTemplate.opsForHash().get(listKey, fieldKey);
        assertNotNull(itemB);
        assertEquals(30L, itemB.getCurrentStock());
        assertEquals(80L, itemB.getTotalStock());

        // 情境 C：扣除過多導致庫存不足 (-50)
        // 目前只有 30，扣 50 會變 -20，應回傳 -1
        Long resultC = redisTemplate.execute(stockScript, Arrays.asList(listKey, fieldKey), 30L, -50L);
        assertEquals(-1L, resultC, "庫存不足應回傳 -1");

        // 驗證 Redis 中的最終狀態 (庫存應維持在 30，且 TotalStock 已更新)
        Item finalItem = (Item) redisTemplate.opsForHash().get(listKey, fieldKey);
        assertNotNull(finalItem);
        assertEquals(30L, finalItem.getCurrentStock(), "庫存不足時不應更新 currentStock");
        assertEquals(80L, finalItem.getTotalStock(), "TotalStock 應已被更新");

        // 情境 d：不存在的item，應回傳 -1
        String nonexistenceFieldKey = "444";
        Long resultD= redisTemplate.execute(stockScript, Arrays.asList(listKey, nonexistenceFieldKey), 110L, 50L);
        assertEquals(-1L, resultC, "不存在的獎品應回傳 -1");
    }
}