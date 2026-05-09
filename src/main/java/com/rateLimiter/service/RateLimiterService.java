package com.rateLimiter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private final Map<String, List<Long>> requestLogs = new HashMap<>();

    @Value("${app.rate-limit.capacity:5}")
    private int capacity;

    @Value("${app.rate-limit.refill-rate:5}")
    private int refillRate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();

        requestLogs.putIfAbsent(key, new ArrayList<>());
        List<Long> logs = requestLogs.get(key);

        logs.add(now);
        logs.removeIf(timestamp -> now - timestamp > 60000); // keep only last 60 seconds

        String redisKey = "ratelimit:" + key;

        String tokensStr = redisTemplate.opsForValue().get(redisKey + ":tokens");
        String timestampStr = redisTemplate.opsForValue().get(redisKey + ":timestamp");

        double tokens = (tokensStr != null) ? Double.parseDouble(tokensStr) : capacity;
        long lastRefill = (timestampStr != null) ? Long.parseLong(timestampStr) : now;

        long secondsPassed = (now - lastRefill) / 1000;
        tokens = Math.min(capacity, tokens + (secondsPassed * refillRate));

        if (tokens >= 1.0) {
            tokens -= 1.0;

            // Save back to Redis
            redisTemplate.opsForValue().set(redisKey + ":tokens", String.valueOf(tokens)); //redis maintains eveyrthing  in string so convert into string form
            redisTemplate.opsForValue().set(redisKey + ":timestamp", String.valueOf(now));

            return true;
        }

        return false;
    }
}