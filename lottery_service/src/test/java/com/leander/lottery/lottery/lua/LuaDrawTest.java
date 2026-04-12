package com.leander.lottery.lottery.lua;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LuaDrawTest {

    private static RedisServer redisServer;
    private StringRedisTemplate redisTemplate;
    private DefaultRedisScript<String> script;
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
        redisTemplate = new StringRedisTemplate(factory);

        // 清空資料
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 載入 Lua 腳本
        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/draw.lua")); // 請確保檔案在 src/test/resources 下
        script.setResultType(String.class);
    }

    @Test
    @DisplayName("測試機率分布與庫存限制")
    void testProbabilityAndStock() throws Exception {
        // 1. 準備資料
        Long campaignId = 111L;
        Long userId = 222L;
        String lotteryCountKey = "{campaign_" + campaignId + "}+_lottery_count_" + campaignId + "_" + userId;
        String campaignItemListKey = "{campaign_" + campaignId + "}+_campaign_itemlist_" + campaignId;

        // 設定使用者有 100 次抽獎機會
        redisTemplate.opsForValue().set(lotteryCountKey, "100");

        // 設定獎品 (Total 100%): 
        // Item A: 20% (Stock: 1), Item B: 30% (Stock: 10), 銘謝惠顧會吃掉剩下的空間
        Map<String, String> items = new HashMap<>();
        items.put("1", "{\"id\":1,\"name\":\"大獎\",\"probability\":20000000,\"currentStock\":1}");
        items.put("2", "{\"id\":2,\"name\":\"二獎\",\"probability\":30000000,\"currentStock\":10}");
        redisTemplate.opsForHash().putAll(campaignItemListKey, items);

        // 2. 模擬隨機數 (Argv[2]...): 
        // 第一抽: 15,000,000 (落在大獎區間 0~20M) -> 應中大獎
        // 第二抽: 15,000,000 (落在大獎區間，但大獎庫存已空) -> 應中「銘謝惠顧」
        // 第三抽: 40,000,000 (落在二獎區間 20M~50M) -> 應中二獎
        // 第四抽: 90,000,000 (落在銘謝惠顧區間 50M~100M) -> 應中「銘謝惠顧」
        List<String> keys = Arrays.asList(lotteryCountKey, campaignItemListKey);
        Object[] args = {"4", "15000000", "15000000", "40000000", "90000000"};

        // 3. 執行 Lua
        String rawResult = redisTemplate.execute(script, keys, args);
        List<Map<String, Object>> results = objectMapper.readValue(rawResult, List.class);

        System.out.println("result:" + results);

        // 4. 驗證
        assertEquals(4, results.size());

        // 第一抽：大獎
        assertEquals(1, results.get(0).get("item_id"));
        assertEquals("大獎", results.get(0).get("item_name"));
        assertTrue((Boolean) results.get(0).get("is_win"));

        // 第二抽：雖然機率落在大獎，但庫存沒了，應為銘謝惠顧
        assertNull(results.get(1).get("item_id"));
        assertEquals("銘謝惠顧", results.get(1).get("item_name"));
        assertFalse((Boolean) results.get(1).get("is_win"));

        // 第三抽：二獎
        assertEquals(2, results.get(2).get("item_id"));
        assertEquals("二獎", results.get(2).get("item_name"));
        assertTrue((Boolean) results.get(2).get("is_win"));

        // 第三抽：銘謝惠顧
        assertNull(results.get(3).get("item_id"));
        assertEquals("銘謝惠顧", results.get(3).get("item_name"));
        assertFalse((Boolean) results.get(3).get("is_win"));

        // 5. 驗證 Redis 狀態
        // 檢查餘額應扣除 3 次
        assertEquals("96", redisTemplate.opsForValue().get(lotteryCountKey));

        // 檢查大獎的庫存應剩 0
        String item1Str = (String) redisTemplate.opsForHash().get(campaignItemListKey, "1");
        assertTrue(item1Str.contains("\"currentStock\":0"));

        // 檢查二獎的庫存應剩 9
        String item2Str = (String) redisTemplate.opsForHash().get(campaignItemListKey, "2");
        assertTrue(item2Str.contains("\"currentStock\":9"));
    }

    @Test
    @DisplayName("測試次數不足時回傳空陣列")
    void testInsufficientCount() {
        redisTemplate.opsForValue().set("count_key", "5");

        String result = redisTemplate.execute(script,
                Arrays.asList("count_key", "item_key"), "10", "1000000");

        assertEquals("[]", result);


        // 1. 準備資料
        Long campaignId = 111L;
        Long userId = 222L;
        String lotteryCountKey = "{campaign_" + campaignId + "}+_lottery_count_" + campaignId + "_" + userId;
        String campaignItemListKey = "{campaign_" + campaignId + "}+_campaign_itemlist_" + campaignId;

        // 設定使用者有 1 次抽獎機會
        redisTemplate.opsForValue().set(lotteryCountKey, "1");

        // 抽 4 次
        List<String> keys = Arrays.asList(lotteryCountKey, campaignItemListKey);
        Object[] args = {"4", "15000000", "15000000", "40000000", "90000000"};

        // 3. 執行 Lua
        String rawResult = redisTemplate.execute(script, keys, args);

        // 4. 驗證
        assertEquals("[]", result);
    }
}