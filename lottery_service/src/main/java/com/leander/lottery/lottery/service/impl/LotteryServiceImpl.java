package com.leander.lottery.lottery.service.impl;

import com.leander.lottery.lottery.dto.*;
import com.leander.lottery.lottery.dto.DrawResponse.DrawResult;
import com.leander.lottery.lottery.entity.Campaign;
import com.leander.lottery.lottery.exception.ExceedMaxTriesException;
import com.leander.lottery.lottery.exception.RemainingCountNotEnoughException;
import com.leander.lottery.lottery.exception.ResourceNotFoundException;
import com.leander.lottery.lottery.service.LotteryService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class LotteryServiceImpl implements LotteryService {

    @Value("${redis.prefix.hashtag}")
    private String hashTagPrefix;

    @Value("${redis.prefix.key.campaign}")
    private String campaignKeyPrefix;

    @Value("${redis.prefix.key.campaign.itemlist}")
    private String campaignItemListKeyPrefix;

    @Value("${redis.prefix.key.lottery.count}")
    private String lotteryCountKeyPrefix;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 建立一個專門記錄抽獎結果的 Logger (對應 Logback 配置中的 LOTTERY_FILE)
    private static final Logger lotteryLogger = LoggerFactory.getLogger("lottery_result_logger");

    // 預載入 Lua Script 以提升效能
    private DefaultRedisScript<String> lotteryScript;

    @PostConstruct
    public void init() {
        lotteryScript = new DefaultRedisScript<>();
        lotteryScript.setResultType(String.class);
        lotteryScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/draw.lua")));
    }

    public DrawResponse executeDraw(Long userId, DrawRequest req) {
        Long campaignId = req.getCampaignId();
        Integer count = req.getCount();

        // 檢查活動狀態與次數限制 (從 Redis 讀取快取)
        String campaignKey = "{" + hashTagPrefix + campaignId + "}_" + campaignKeyPrefix + campaignId;
        String campaignStr = redisTemplate.opsForValue().get(campaignKey);

        log.info("campaignStr:" + campaignStr);

        // 驗證活動是否存在
        if (campaignStr == null) {
            throw new ResourceNotFoundException("No this campaign");
        }

        Campaign campaign = objectMapper.readValue(campaignStr, Campaign.class);

        // 驗證活動時間
        if (System.currentTimeMillis() >= campaign.getEndTime()) {
            throw new ResourceNotFoundException("Campaign has ended");
        }

        // 驗證單次抽獎次數上限
        if (count > campaign.getMaxTries()) {
            throw new ExceedMaxTriesException("Exceed max tries");
        }

        // 準備抽獎
        ArrayList<String> keys = new ArrayList<>();
        String lotteryCountKey = "{" + hashTagPrefix + campaignId + "}_" + lotteryCountKeyPrefix + campaignId + "_" + userId;
        String campaignItemListKey = "{" + hashTagPrefix + campaignId + "}_" + campaignItemListKeyPrefix + campaignId;
        keys.add(lotteryCountKey);
        keys.add(campaignItemListKey);

        ArrayList<String> args = new ArrayList<>();
        args.add(String.valueOf(req.getCount()));

        // 生成與 count 數量一致的隨機數 (0 ~ 99,999,999)
        for (int i = 0; i < req.getCount(); i++) {
            Integer randomValue = ThreadLocalRandom.current().nextInt(100000000);
            args.add(String.valueOf(randomValue));

            log.info("random value " + i + ":" + randomValue);
        }

        // 執行抽獎
        String result = redisTemplate.execute(
                lotteryScript,
                keys,
                args.toArray()
        );

        log.info("redis result:" + result);

        ArrayList<DrawResult> results = objectMapper.readValue(result, new TypeReference<>() {
        });

        // 判斷是否有回傳獎品列表
        if (results.isEmpty()) {
            throw new RemainingCountNotEnoughException("Remaining count is not enough");
        }

        // write data into locals
        recordDrawResult(results);

        return new DrawResponse(results);
    }

    public void recordDrawResult(ArrayList<DrawResult> results) {
        // 1. 判斷傳入列表是否為空
        if (results == null || results.isEmpty()) {
            return;
        }

        // 2. 使用 for-each 迴圈逐筆處理
        for (DrawResult result : results) {
            try {
                // 在寫入 Log 前生成 UUID，確保這一筆抽獎紀錄在整個系統中唯一
                // 即使 Kafka 重新發送這一行，Consumer 也能透過這個 ID 判斷已處理過
                result.setDrawId(java.util.UUID.randomUUID().toString());

                // 改用 ObjectMapper 將物件轉換為 JSON 字串
                String logLine = objectMapper.writeValueAsString(result);

                // 寫入本地 Log 文件
                lotteryLogger.info(logLine);
            } catch (Exception e) {
                // 這裡記錄錯誤，但不中斷其餘結果的紀錄
                log.error("write draw result failed, result:" + result);
            }
        }
    }
}
