package com.taskflow.backend.ratelimit;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RateLimiter}'s fixed-window counting (Issue #3).
 *
 * <p>Uses a short window ({@code 150ms}) with a real {@code Thread.sleep} to
 * exercise the reset path deterministically without mocking the clock.
 */
class RateLimiterTest {

    private final RateLimiter rateLimiter = new RateLimiter();

    @Test
    void allowsRequestsUpToTheConfiguredLimit() {
        String key = "1.2.3.4:login";
        Duration window = Duration.ofMinutes(1);

        assertThat(rateLimiter.tryConsume(key, 3, window)).isTrue();
        assertThat(rateLimiter.tryConsume(key, 3, window)).isTrue();
        assertThat(rateLimiter.tryConsume(key, 3, window)).isTrue();
    }

    @Test
    void rejectsRequestsBeyondTheConfiguredLimit() {
        String key = "1.2.3.4:login";
        Duration window = Duration.ofMinutes(1);

        rateLimiter.tryConsume(key, 2, window);
        rateLimiter.tryConsume(key, 2, window);

        assertThat(rateLimiter.tryConsume(key, 2, window)).isFalse();
        assertThat(rateLimiter.tryConsume(key, 2, window)).isFalse();
    }

    @Test
    void resetsAndAllowsRequestsAgainAfterTheWindowElapses() throws InterruptedException {
        String key = "1.2.3.4:login";
        Duration window = Duration.ofMillis(150);

        rateLimiter.tryConsume(key, 1, window);
        assertThat(rateLimiter.tryConsume(key, 1, window)).isFalse();

        Thread.sleep(200);

        assertThat(rateLimiter.tryConsume(key, 1, window)).isTrue();
    }

    @Test
    void differentKeysHaveIndependentBudgets() {
        Duration window = Duration.ofMinutes(1);

        assertThat(rateLimiter.tryConsume("1.2.3.4:login", 1, window)).isTrue();
        assertThat(rateLimiter.tryConsume("1.2.3.4:login", 1, window)).isFalse();

        // A different IP, and the same IP on a different endpoint, are unaffected.
        assertThat(rateLimiter.tryConsume("5.6.7.8:login", 1, window)).isTrue();
        assertThat(rateLimiter.tryConsume("1.2.3.4:register", 1, window)).isTrue();
    }
}
