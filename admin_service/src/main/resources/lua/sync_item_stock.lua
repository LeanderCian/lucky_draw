-- 同步item stock至redis

-- KEYS: [campaignItemListKey, itemHashKey]
-- ARGV: [totalStock, incrementAmount]

local campaignItemListKey = KEYS[1]
local itemHashKey = KEYS[2]
local newTotalStock = ARGV[1]
local incrementAmount = ARGV[2];
local newCurrentStock = -1;

-- 1. 取得當前 Redis 中的庫存
local itemJson = redis.call('HGET', campaignItemListKey, itemHashKey)
if itemJson then
    local decoded = cjson.decode(itemJson)
    newCurrentStock = decoded['currentStock'] + incrementAmount

    -- 2. 檢查更新後是否會小於 0
    if newCurrentStock < 0 then
        return -1 -- 回傳 -1 代表庫存不足
    end
end

-- 3. 更新 Redis 配置中的物件 (Hash Key)
local itemJson = redis.call('HGET', campaignItemListKey, itemHashKey)
if itemJson then
    local decoded = cjson.decode(itemJson)
    decoded['totalStock'] = newTotalStock
    decoded['currentStock'] = newCurrentStock
    redis.call('HSET', campaignItemListKey, itemHashKey, cjson.encode(decoded))
end

-- 回傳更新後的正確庫存
return newCurrentStock