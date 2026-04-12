package com.leander.lottery.lottery.util;

public class LuaUtils {
    // 同步item至redis
    // KEYS: [campaignItemListKey, itemHashKey]
    // ARGV: [item]
    // note: sync stock to redis only in create item case,
    //       if want update stock in redis, please use SYNC_ITEM_STOCK_TO_REDIS_LUA
    public static final String SYNC_ITEM_TO_REDIS_LUA =
            "redis.call('HSET', KEYS[1], KEYS[2], ARGV[1]); " + // 更新獎品清單
                    "return true;";

    // 同步item stock至redis
    // KEYS: [campaignItemListKey, itemHashKey]
    // ARGV: [totalStock, incrementAmount]
    public static final String SYNC_ITEM_STOCK_TO_REDIS_LUA =
            "local campaignItemListKey = KEYS[1]; " +
                    "local itemHashKey = KEYS[2]; " +
                    "local newTotalStock = ARGV[1]; " +
                    "local incrementAmount = ARGV[2]; " +
                    "local newCurrentStock = -1; " +

                    // 1. 取得當前 Redis 中的庫存
                    "local itemJson = redis.call('HGET', campaignItemListKey, itemHashKey); " +
                    "if itemJson then " +
                    "  local decoded = cjson.decode(itemJson); " +
                    "  newCurrentStock = decoded['currentStock'] + incrementAmount; " +
                    // 2. 檢查更新後是否會小於 0
                    "  if newCurrentStock < 0 then " +
                    "    return -1; " + // 回傳 -1 代表庫存不足
                    "  end; " +
                    "end; " +

                    // 3. 更新 Redis 配置中的物件 (Hash Key)
                    "local itemJson = redis.call('HGET', campaignItemListKey, itemHashKey); " +
                    "if itemJson then " +
                    "  local decoded = cjson.decode(itemJson); " +
                    "  decoded['totalStock'] = newTotalStock; " +
                    "  decoded['currentStock'] = newCurrentStock; " +
                    "  redis.call('HSET', campaignItemListKey, itemHashKey, cjson.encode(decoded)); " +
                    "end; " +

                    "return newCurrentStock;"; // 回傳更新後的正確庫存

    // 同步user remaining count至redis
    // KEYS: [lotteryCountKey]
    // ARGV: [incrementAmount]
    // note: sync stock to redis only in create item case,
    //       if want update stock in redis, please use SYNC_ITEM_STOCK_TO_REDIS_LUA
    public static final String SYNC_LOTTERY_COUNT_TO_REDIS_LUA =
            "local lotteryCountKey = KEYS[1]; " +
                    "local incrementAmount = ARGV[1]; " +

                    // 1. 取得當前 Redis 中的remaining count
                    "local remainingCount = redis.call('GET', lotteryCountKey) or '0'; " +
                    "local newRemainingCount = remainingCount + incrementAmount; " +

                    // 2. 更新 Redis 中的remaining count
                    "redis.call('SET', lotteryCountKey, newRemainingCount); " +

                    "return true;";

}
