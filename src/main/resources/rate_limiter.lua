-- KEYS[1]: voucher limiter key
-- KEYS[2]: user limiter key
-- ARGV[1]: voucher capacity
-- ARGV[2]: voucher refill tokens per second
-- ARGV[3]: user capacity
-- ARGV[4]: user refill tokens per second
-- ARGV[5]: requested tokens

local function consume_token(key, capacity, refill_per_second, requested, now_ms)
    local data = redis.call('hmget', key, 'tokens', 'ts')
    local tokens = tonumber(data[1])
    local ts = tonumber(data[2])

    if tokens == nil then
        tokens = capacity
    end
    if ts == nil then
        ts = now_ms
    end

    local elapsed = now_ms - ts
    if elapsed > 0 then
        local refill = (elapsed / 1000.0) * refill_per_second
        tokens = math.min(capacity, tokens + refill)
    end

    if tokens < requested then
        redis.call('hset', key, 'tokens', tokens, 'ts', now_ms)
        redis.call('pexpire', key, 60000)
        return 0
    end

    tokens = tokens - requested
    redis.call('hset', key, 'tokens', tokens, 'ts', now_ms)
    redis.call('pexpire', key, 60000)
    return 1
end

local voucher_key = KEYS[1]
local user_key = KEYS[2]
local voucher_capacity = tonumber(ARGV[1])
local voucher_refill = tonumber(ARGV[2])
local user_capacity = tonumber(ARGV[3])
local user_refill = tonumber(ARGV[4])
local requested = tonumber(ARGV[5])

local now = redis.call('time')
local now_ms = (tonumber(now[1]) * 1000) + math.floor(tonumber(now[2]) / 1000)

if consume_token(voucher_key, voucher_capacity, voucher_refill, requested, now_ms) == 0 then
    return 1
end

if consume_token(user_key, user_capacity, user_refill, requested, now_ms) == 0 then
    return 2
end

return 0
