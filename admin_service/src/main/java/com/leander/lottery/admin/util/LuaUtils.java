package com.leander.lottery.admin.util;

public class LuaUtils {
    // 同步item至redis
    // KEYS: [itemStockKey, campaignItemListKey, itemHashKey]
    // ARGV: [totalStock, item]
    // note: sync stock to redis only in create item case,
    //       if want update stock in redis, please use SYNC_ITEM_STOCK_TO_REDIS_LUA
    public static final String SYNC_ITEM_TO_REDIS_LUA =
            "redis.call('SET', KEYS[1], ARGV[1]); " +  // 更新庫存
                    "redis.call('HSET', KEYS[2], KEYS[3], ARGV[2]); " + // 更新獎品清單
                    "return true;";

    // 同步item stock至redis
    // KEYS: [itemStockKey, campaignItemListKey, itemHashKey]
    // ARGV: [totalStock, incrementAmount]
    public static final String SYNC_ITEM_STOCK_TO_REDIS_LUA =
            "local itemStockKey = KEYS[1]; " +
                    "local campaignItemListKey = KEYS[2]; " +
                    "local itemHashKey = KEYS[3]; " +
                    "local newTotalStock = ARGV[1]; " +
                    "local incrementAmount = ARGV[2]; " +

                    // 1. 取得當前 Redis 中的庫存
                    "local currentStock = redis.call('GET', itemStockKey) or '0'; " +
                    "local newCurrentStock = currentStock + incrementAmount; " +

                    // 2. 檢查更新後是否會小於 0
                    "if newCurrentStock < 0 then " +
                    "  return -1; " + // 回傳 -1 代表庫存不足
                    "end; " +

                    // 3. 更新 Redis 庫存 (String Key)
                    "redis.call('SET', itemStockKey, newCurrentStock); " +

                    // 4. 更新 Redis 配置中的物件 (Hash Key)
                    "local itemJson = redis.call('HGET', campaignItemListKey, itemHashKey); " +
                    "if itemJson then " +
                    "  local decoded = cjson.decode(itemJson); " +
                    "  decoded['totalStock'] = newTotalStock; " +
                    "  decoded['currentStock'] = newCurrentStock; " +
                    "  redis.call('HSET', campaignItemListKey, itemHashKey, cjson.encode(decoded)); " +
                    "end; " +

                    "return newCurrentStock;"; // 回傳更新後的正確庫存
}
