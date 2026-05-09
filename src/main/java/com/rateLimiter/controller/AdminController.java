package com.rateLimiter.controller;

import com.rateLimiter.service.RateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final RateLimiterService rateLimiterService;

    public AdminController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/update-limits")
    public ResponseEntity<Map<String, String>> updateLimits(
            @RequestParam int capacity,
            @RequestParam int refillRate) {

        rateLimiterService.updateLimits(capacity, refillRate);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Rate limit updated successfully");
        response.put("newCapacity", String.valueOf(capacity));
        response.put("newRefillRate", String.valueOf(refillRate));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-limits")
    public ResponseEntity<Map<String, Integer>> getCurrentLimits() {

        Map<String, Integer> limits = new HashMap<>();
        limits.put("defaultCapacity", rateLimiterService.getCurrentCapacity());
        limits.put("defaultRefillRate", rateLimiterService.getCurrentRefillRate());
        return ResponseEntity.ok(limits);
    }
}