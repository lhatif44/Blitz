package com.blitz.config;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class RateLimiter {

    private record Window(long windowStart, AtomicInteger count) {
    }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimiter() {
        this(Clock.systemUTC());
    }

    // package-private constructor lets tests inject a controllable clock
    RateLimiter(Clock clock) {
        this.clock = clock;
    }

    // Returns true if this call is within the allowed rate (and counts it against the window),
    // false if the key has already exceeded maxAttempts within the current windowMillis window.
    public boolean allow(String key, int maxAttempts, long windowMillis) {
        long now = clock.millis();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart() >= windowMillis) {
                return new Window(now, new AtomicInteger(0));
            }
            return existing;
        });
        return window.count().incrementAndGet() <= maxAttempts;
    }
}
