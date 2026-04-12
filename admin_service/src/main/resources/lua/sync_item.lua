-- 同步item至redis

-- KEYS: [campaignItemListKey, itemHashKey]
-- ARGV: [item]
-- note: sync stock to redis only in create item case,
-- if want update stock in redis, please use SYNC_ITEM_STOCK_TO_REDIS_LUA

redis.call('HSET', KEYS[1], KEYS[2], ARGV[1]);

return true