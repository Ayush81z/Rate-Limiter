package com.rateLimiter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.capacity:5}")
    private int capacity;           // Max tokens (bucket size)

    @Value("${app.rate-limit.refill-rate:5}")
    private int refillRate;         // Tokens added per minute

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


//      Checks whether the request is allowed or not
//      @param key  (IP address or user ID)
//      @return true = allowed, false = blocked


    public boolean isAllowed(String key) {

        String redisKey = "ratelimit:" + key;
        long now = System.currentTimeMillis();

//         Get current tokens
        String tokensStr = redisTemplate.opsForValue().get(redisKey + ":tokens");
//        last refill time from Redis
        String timestampStr = redisTemplate.opsForValue().get(redisKey + ":timestamp");

        // Set default values if first time
        double tokens = tokensStr != null ? Double.parseDouble(tokensStr) : capacity;
        long lastRefillTime = timestampStr != null ? Long.parseLong(timestampStr) : now;

//        refill tokens based on time passed
        long timePassedInSeconds = (now - lastRefillTime) / 1000;
        tokens = Math.min(capacity, tokens + (timePassedInSeconds * refillRate));

//        Check if we have at least 1 token
        if (tokens >= 1) {
            tokens = tokens - 1;   // Consume one token

//            Save updated values back to Redis
            redisTemplate.opsForValue().set(redisKey + ":tokens", String.valueOf(tokens));
            redisTemplate.opsForValue().set(redisKey + ":timestamp", String.valueOf(now));

            return true;   // Request Allowed if the tokens are > 1
        }

        return false;  // Rate limit exceeded then block the request
    }
}