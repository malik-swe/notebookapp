package com.example.notebookapp.security.ratelimit;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final SimpleRateLimiter limiter;
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    public RateLimitFilter(SimpleRateLimiter limiter) {
        this.limiter = limiter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String ip = request.getRemoteAddr();

        if (!limiter.allow(ip)) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            log.warn("Rate limit exceeded");
            return;
        }

        chain.doFilter(request, response);
    }
}
