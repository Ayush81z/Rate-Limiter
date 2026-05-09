package com.rateLimiter.filter;

import com.rateLimiter.service.RateLimiterService;
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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Get client IP address
        String clientIp = request.getRemoteAddr();

        // You can also combine IP + endpoint for more strict limiting
        String endpoint = request.getRequestURI();
        String key = clientIp + ":" + endpoint;

        // Check if request is allowed
        boolean allowed = rateLimiterService.isAllowed(key);

        if (!allowed) {
            // Send 429 errror Too Many Requests response
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too Many Requests. Please try again later.\"}");
            return; // Stop further processing
        }

        // If allowed, continue to the controller
        filterChain.doFilter(request, response);
    }
}