package com.example.notebookapp.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private static final int LIMIT = 100; // requests
    private static final long WINDOW_MS = 60_000;

    private final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        counters.putIfAbsent(ip, new RequestCounter(0, now));
        RequestCounter counter = counters.get(ip);

        synchronized (counter) {
            if (now - counter.startTime > WINDOW_MS) {
                counter.count = 0;
                counter.startTime = now;
            }

            counter.count++;
            if (counter.count > LIMIT) {
                response.setStatus(429);
                response.getWriter().write("Too many requests");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private static class RequestCounter {
        int count;
        long startTime;

        RequestCounter(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }
    }
}
