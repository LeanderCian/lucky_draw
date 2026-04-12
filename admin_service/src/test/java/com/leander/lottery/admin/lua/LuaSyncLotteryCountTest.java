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

class LuaSyncLotteryCountTest {

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
        script.setLocation(new ClassPathResource("lua/sync_lottery_count.lua"));
        script.setResultType(Long.class);
    }

    @Test
    void testSyncLotteryCount() throws Exception {
        // 驗證 create lottery count
        // 1. 準備資料
        Long campaignId = 111L;
        Long userId = 222L;
        Integer incrementAmount = 10;

        String lotteryCountKey = "{campaign_" + campaignId + "}_lottery_count_" + campaignId + "_" + userId;
        List<String> keys = Arrays.asList(lotteryCountKey);

        // 2. 執行 Lua
        redisTemplate.execute(script, keys, incrementAmount);

        // 3. 驗證
        // 從 Redis 抓回資料驗證
        Integer result = (Integer) redisTemplate.opsForValue().get(lotteryCountKey);
        assertEquals(10, result);

        // 驗證 update lottery count
        // 1. 執行 Lua
        redisTemplate.execute(script, keys, incrementAmount);

        // 2. 驗證
        // 從 Redis 抓回資料驗證
        Integer newResult = (Integer) redisTemplate.opsForValue().get(lotteryCountKey);
        assertEquals(20, newResult);
    }
}