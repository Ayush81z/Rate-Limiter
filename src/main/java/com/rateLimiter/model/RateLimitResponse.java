package com.rateLimiter.model;

public class RateLimitResponse {

    private boolean allowed;
    private double remainingTokens;

    public RateLimitResponse(boolean allowed, double remainingTokens) {
        this.allowed = allowed;
        this.remainingTokens = remainingTokens;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public double getRemainingTokens() {
        return remainingTokens;
    }
}