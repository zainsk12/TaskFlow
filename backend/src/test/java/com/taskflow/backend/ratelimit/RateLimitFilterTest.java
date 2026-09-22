package com.taskflow.backend.ratelimit;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RateLimitFilter} (Issue #3): which routes it applies
 * to, that it enforces the configured per-endpoint limits, and that it stays
 * out of the way of everything else.
 *
 * <p>Exercises the filter directly against a real {@link RateLimiter} (fast,
 * deterministic, no Spring context needed) rather than through the full
 * security filter chain.
 */
class RateLimitFilterTest {

    private static final String REMOTE_IP = "203.0.113.7";

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        RateLimitProperties properties = new RateLimitProperties(
                true, Duration.ofMinutes(1), /* login */ 2, /* register */ 2, /* refresh */ 3);
        filter = new RateLimitFilter(new RateLimiter(), properties);
    }

    @Test
    void allowsLoginRequestsWithinTheLimit() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(loginRequest(), new MockHttpServletResponse(), chain);
        filter.doFilter(loginRequest(), new MockHttpServletResponse(), chain);

        verify(chain, times(2)).doFilter(any(), any());
    }

    @Test
    void rejectsLoginRequestsBeyondTheLimitWith429() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(loginRequest(), new MockHttpServletResponse(), chain);
        filter.doFilter(loginRequest(), new MockHttpServletResponse(), chain);

        MockHttpServletResponse rejected = new MockHttpServletResponse();
        filter.doFilter(loginRequest(), rejected, chain);

        assertThat(rejected.getStatus()).isEqualTo(429);
        // Compare as a parsed media type, not a literal string: the servlet container
        // appends ";charset=UTF-8" (from our explicit setCharacterEncoding call) to
        // getContentType(), which is still plain JSON — MediaType comparison ignores that.
        assertThat(MediaType.parseMediaType(rejected.getContentType()).isCompatibleWith(MediaType.APPLICATION_JSON))
                .isTrue();
        assertThat(rejected.getContentAsString()).contains("\"status\":429").contains("Too many requests");
        verify(chain, times(2)).doFilter(any(), any()); // the 3rd request never reached the chain
    }

    @Test
    void registrationAndRefreshHaveIndependentLimitsFromLogin() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        // Exhaust login's budget (2)...
        filter.doFilter(loginRequest(), new MockHttpServletResponse(), chain);
        filter.doFilter(loginRequest(), new MockHttpServletResponse(), chain);
        MockHttpServletResponse loginBlocked = new MockHttpServletResponse();
        filter.doFilter(loginRequest(), loginBlocked, chain);
        assertThat(loginBlocked.getStatus()).isEqualTo(429);

        // ...registration (also 2) is still fully available for the same IP.
        MockHttpServletResponse registerOk = new MockHttpServletResponse();
        filter.doFilter(postRequest("/api/v1/auth/register"), registerOk, chain);
        assertThat(registerOk.getStatus()).isEqualTo(200);
    }

    @Test
    void nonAuthEndpointsAreNeverRateLimited() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = postRequest("/api/v1/tasks");

        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }

        verify(chain, times(10)).doFilter(any(), any());
    }

    @Test
    void logoutIsNotRateLimited() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = postRequest("/api/v1/auth/logout");

        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }

        verify(chain, times(10)).doFilter(any(), any());
    }

    @Test
    void getRequestsToAuthPathsAreNotRateLimited() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        request.setRemoteAddr(REMOTE_IP);

        for (int i = 0; i < 5; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }

        verify(chain, times(5)).doFilter(any(), any());
    }

    @Test
    void disablingRateLimitingLetsEverythingThrough() throws Exception {
        RateLimitProperties disabled = new RateLimitProperties(false, Duration.ofMinutes(1), 1, 1, 1);
        RateLimitFilter disabledFilter = new RateLimitFilter(new RateLimiter(), disabled);
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 5; i++) {
            disabledFilter.doFilter(loginRequest(), new MockHttpServletResponse(), chain);
        }

        verify(chain, times(5)).doFilter(any(), any());
    }

    private MockHttpServletRequest loginRequest() {
        return postRequest("/api/v1/auth/login");
    }

    private MockHttpServletRequest postRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr(REMOTE_IP);
        return request;
    }
}
