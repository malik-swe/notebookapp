package com.example.notebookapp.security.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimpleRateLimiter {

    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        long now = Instant.now().toEpochMilli();
        Counter c = counters.computeIfAbsent(key, k -> new Counter(now));

        synchronized (c) {
            if (now - c.start > WINDOW_MS) {
                c.start = now;
                c.count = 0;
            }
            c.count++;
            return c.count <= MAX_REQUESTS;
        }
    }

    private static class Counter {
        long start;
        int count = 0;

        Counter(long start) {
            this.start = start;
        }
    }
}
