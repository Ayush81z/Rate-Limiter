package com.rateLimiter.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import com.rateLimiter.model.RateLimitResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private int currentCapacity = 5;
    private int currentRefillRate = 5;

    private final Map<String, List<Long>> requestLogs = new HashMap<>();

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Updated to clear old Redis data when limits change
    public void updateLimits(int capacity, int refillRate) {
        this.currentCapacity = capacity;
        this.currentRefillRate = refillRate;

        // Clear all existing rate limit data in Redis so new limits take effect
        redisTemplate.delete(redisTemplate.keys("ratelimit:*"));

        System.out.println(" Rate limit updated successfully to: " + capacity + " requests/min");
    }

    public RateLimitResponse isAllowed(String key) {

        long now = System.currentTimeMillis();

        String redisKey = "ratelimit:" + key;

        String tokensStr = redisTemplate.opsForValue().get(redisKey + ":tokens");
        String timestampStr = redisTemplate.opsForValue().get(redisKey + ":timestamp");

        double tokens;
        long lastRefillTime;

        if (tokensStr == null || timestampStr == null) {
            tokens = currentCapacity;
            lastRefillTime = now;
        } else {
            tokens = Double.parseDouble(tokensStr);
            lastRefillTime = Long.parseLong(timestampStr);
        }

        // Time passed in seconds
        long timePassed = (now - lastRefillTime) / 60000;

        // Add tokens based on elapsed time
        if (timePassed > 0) {
            tokens = Math.min(currentCapacity,
                    tokens + (timePassed * currentRefillRate));

            lastRefillTime = now;
        }

        // Consume token for each request
        if (tokens >= 1) {

            tokens--;

            redisTemplate.opsForValue().set(redisKey + ":tokens", String.valueOf(tokens));
            redisTemplate.opsForValue().set(redisKey + ":timestamp", String.valueOf(lastRefillTime));

            return new RateLimitResponse(true, tokens);
        }

        return new RateLimitResponse(false, tokens);
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public int getCurrentRefillRate() {
        return currentRefillRate;
    }
}