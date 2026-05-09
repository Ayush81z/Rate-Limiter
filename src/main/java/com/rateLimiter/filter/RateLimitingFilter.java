package com.rateLimiter.filter;

import com.rateLimiter.service.RateLimiterService;
import com.rateLimiter.model.RateLimitResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    public RateLimitingFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();

        response.setHeader("X-Client-IP", clientIp);

        // Bypass admin endpoints
        if (endpoint.startsWith("/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientIp + ":" + endpoint;

        System.out.println("Rate Limit Check -> IP: " + clientIp + " | Endpoint: " + endpoint + " | Key: " + key);

        RateLimitResponse result = rateLimiterService.isAllowed(key);

        response.setHeader("X-RateLimit-Limit",
                String.valueOf(rateLimiterService.getCurrentCapacity()));

        response.setHeader("X-RateLimit-Remaining",
                String.valueOf((int) result.getRemainingTokens()));

        if (!result.isAllowed()) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too Many Requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // Helper method to get real client IP
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}