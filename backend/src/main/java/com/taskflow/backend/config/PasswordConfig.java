package com.taskflow.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password hashing configuration.
 *
 * <p>Exposes a single {@link PasswordEncoder} bean ({@link BCryptPasswordEncoder})
 * used to hash passwords at registration and verify them at login. Kept separate
 * from any web-security setup so it is available before {@code SecurityConfig}
 * exists (added in the JWT phase).
 */
@Configuration
public class PasswordConfig {

    /**
     * BCrypt password encoder. BCrypt embeds a per-hash salt and a tunable work
     * factor, so identical passwords produce different hashes and verification is
     * done via {@link PasswordEncoder#matches(CharSequence, String)}.
     *
     * @return the application-wide password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
