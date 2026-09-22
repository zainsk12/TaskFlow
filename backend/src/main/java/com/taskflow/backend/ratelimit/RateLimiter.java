package com.taskflow.backend.ratelimit;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A simple in-memory, fixed-window request counter used to rate-limit the
 * authentication endpoints (Issue #3).
 *
 * <p><b>Limitation:</b> state is held in a local {@link ConcurrentHashMap} and is
 * therefore per-instance only. If the backend is ever scaled to run multiple
 * deployed instances behind a load balancer, each instance enforces its own
 * independent limit — this is NOT a globally distributed rate limiter, and a
 * client could get up to {@code (limit × instance count)} requests through per
 * window. A single Render instance (the current deployment) is unaffected.
 * Sharing state across instances would require an external store (e.g. Redis),
 * which was deliberately not introduced here per the issue's scope.
 *
 * <p>Algorithm: fixed window, not a true sliding log — each key gets a counter
 * that resets the moment {@code window} has elapsed since it was first touched.
 * This is a deliberate simplification (no per-request timestamp history), which
 * can allow a short burst near a window boundary; that tradeoff is acceptable
 * for blunting brute-force/credential-stuffing traffic, which this exists for.
 */
@Component
public class RateLimiter {

    /** Buckets idle for longer than this are assumed abandoned and swept. */
    private static final long STALE_BUCKET_MILLIS = Duration.ofMinutes(30).toMillis();
    private static final long CLEANUP_INTERVAL_MINUTES = 5;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "rate-limit-cleanup");
        thread.setDaemon(true);
        return thread;
    });

    public RateLimiter() {
        cleanupExecutor.scheduleAtFixedRate(
                this::evictStaleBuckets, CLEANUP_INTERVAL_MINUTES, CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Attempts to consume one request from {@code key}'s budget.
     *
     * @param key         the rate-limit key (see {@link RateLimitFilter})
     * @param maxRequests max requests allowed per window
     * @param window      the window duration
     * @return {@code true} if the request is allowed (and now counted); {@code false}
     *         if {@code key} has already exhausted its budget for the current window
     */
    public boolean tryConsume(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        long windowMillis = window.toMillis();
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(now));

        synchronized (bucket) {
            if (now - bucket.windowStart >= windowMillis) {
                bucket.windowStart = now;
                bucket.count = 0;
            }
            if (bucket.count < maxRequests) {
                bucket.count++;
                return true;
            }
            return false;
        }
    }

    private void evictStaleBuckets() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> {
            Bucket bucket = entry.getValue();
            synchronized (bucket) {
                return now - bucket.windowStart > STALE_BUCKET_MILLIS;
            }
        });
    }

    @PreDestroy
    void shutdown() {
        cleanupExecutor.shutdownNow();
    }

    /** Mutable per-key counter; access is synchronized on the instance itself. */
    private static final class Bucket {
        private long windowStart;
        private int count;

        private Bucket(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
