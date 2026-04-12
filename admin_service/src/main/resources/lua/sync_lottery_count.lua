-- 同步user remaining count至redis

-- KEYS: [lotteryCountKey]
-- ARGV: [incrementAmount]

local lotteryCountKey = KEYS[1]
local incrementAmount = ARGV[1]

-- 1. 取得當前 Redis 中的remaining count
local remainingCount = redis.call('GET', lotteryCountKey) or '0'
local newRemainingCount = remainingCount + incrementAmount

-- 2. 更新 Redis 中的remaining count
redis.call('SET', lotteryCountKey, newRemainingCount)

return true