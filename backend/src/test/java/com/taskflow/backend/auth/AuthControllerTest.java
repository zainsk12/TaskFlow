package com.taskflow.backend.auth;

import com.taskflow.backend.auth.dto.AuthResponse;
import com.taskflow.backend.common.Role;
import com.taskflow.backend.ratelimit.RateLimitProperties;
import com.taskflow.backend.ratelimit.RateLimiter;
import com.taskflow.backend.security.JwtService;
import com.taskflow.backend.security.RefreshCookieProperties;
import com.taskflow.backend.user.dto.UserResponse;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link AuthController}'s refresh-token cookie handling
 * (Issue #2). {@link AuthService} is mocked, so these only verify the
 * HTTP/cookie contract — {@code AuthServiceTest} covers the Issue #1
 * revocation logic underneath {@code /refresh} and {@code /logout}.
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RefreshCookieProperties cookieProperties;

    // @WebMvcTest auto-detects Filter beans (see RateLimitFilter, Issue #3) in
    // addition to the sliced controller, so its constructor dependencies must
    // be satisfiable here too — even though addFilters=false means the filter
    // never actually runs during these requests. Mocked for bean creation
    // only, same as JwtService/RefreshCookieProperties above.
    @MockitoBean
    private RateLimiter rateLimiter;

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    @Test
    void loginSetsHttpOnlyCookieAndOmitsRefreshTokenFromBody() throws Exception {
        UserResponse user = new UserResponse(
                "user-1", "Test User", "test@example.com", Role.USER,
                null, "Asia/Kolkata", Instant.now(), Instant.now());
        AuthResponse serviceResponse = AuthResponse.of("access-jwt", "refresh-jwt", 900L, user);

        when(authService.login(any())).thenReturn(serviceResponse);
        when(jwtService.getRefreshTokenExpiresInSeconds()).thenReturn(604_800L);
        when(cookieProperties.secure()).thenReturn(false);
        when(cookieProperties.sameSite()).thenReturn("Lax");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"test@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-jwt"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string("Set-Cookie", allOf(
                        containsString("refreshToken=refresh-jwt"),
                        containsString("HttpOnly"),
                        containsString("Path=/api/v1/auth"),
                        containsString("SameSite=Lax"),
                        not(containsString("Secure")))));
    }

    @Test
    void refreshReadsTokenFromCookieNotBody() throws Exception {
        when(authService.refresh(any())).thenReturn(AuthResponse.accessOnly("new-access-jwt", 900L));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", "cookie-refresh-jwt")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-jwt"));
    }

    @Test
    void refreshWithoutCookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutClearsTheRefreshCookie() throws Exception {
        when(cookieProperties.secure()).thenReturn(false);
        when(cookieProperties.sameSite()).thenReturn("Lax");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie("refreshToken", "cookie-refresh-jwt")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", allOf(
                        containsString("refreshToken="),
                        containsString("Max-Age=0"))));
    }
}
