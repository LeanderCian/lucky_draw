package com.leander.lottery.admin.util;

public class LuaUtils {
    // KEYS: [itemStockKey, campaignItemListKey, itemHashKey]
    // ARGV: [totalStock, item]
    public static final String INSERT_ITEM_LUA =
            "redis.call('SET', KEYS[1], ARGV[1]); " +  // 更新庫存
                    "redis.call('HSET', KEYS[2], KEYS[3], ARGV[2]); " + // 更新獎品清單
                    "return true;";
}
