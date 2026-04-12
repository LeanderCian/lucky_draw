-- 抽獎script

-- KEYS[1]: lotteryCountKey
-- KEYS[2]: campaignItemListKey
-- ARGV[1]: drawCount (連抽次數)
-- ARGV[2]: randomSeeds (傳入多個隨機數，避免 Lua 內隨機性問題)

local lotteryCountKey = KEYS[1]
local campaignItemListKey = KEYS[2]
local drawCount = tonumber(ARGV[1])
local remainingCount = tonumber(redis.call('GET', lotteryCountKey) or "0")

-- 1. 檢查使用者剩餘次數
if remainingCount < drawCount then
    return "[]"
end

-- 2. 取得 Hash 中所有的獎品 JSON 字串
local itemJsons = redis.call('HVALS', campaignItemListKey)
local items = {}
for i, jsonStr in ipairs(itemJsons) do
    items[i] = cjson.decode(jsonStr)
end

local results = {}

-- 3. 執行抽獎迴圈
for i = 1, drawCount do
    local rand = tonumber(ARGV[i + 1])
    local cumulative = 0
    local winItem = nil

    -- 判定隨機數落點
    for _, item in ipairs(items) do
        cumulative = cumulative + tonumber(item.probability)
        if rand < cumulative then
            -- 檢查該獎品在 Hash 內的 currentStock
            -- 注意：這裡我們直接操作 Hash 內的 JSON 以確保一致性
            if tonumber(item.currentStock) > 0 then
                winItem = item
                break
            else
                break
            end
        end
    end

    if winItem then
        -- 4. 扣減該獎品在 Hash 內的庫存
        winItem.currentStock = winItem.currentStock - 1
        -- 將更新後的物件寫回 Hash (Field 是獎品的 id)
        redis.call('HSET', campaignItemListKey, winItem.id, cjson.encode(winItem))

        table.insert(results, {
            item_id = winItem.id,
            item_name = winItem.name,
            is_win = true
        })
    else
        -- 沒中獎或庫存不足
        table.insert(results, {
            item_id = nil,
            item_name = "銘謝惠顧",
            is_win = false
        })
    end
end

-- 5. 扣除使用者剩餘次數
redis.call('DECRBY', lotteryCountKey, drawCount)

return cjson.encode(results)