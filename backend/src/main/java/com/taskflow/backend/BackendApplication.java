package com.taskflow.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * Application entry point.
 *
 * <p>{@link UserDetailsServiceAutoConfiguration} is excluded because authentication
 * is fully stateless/JWT-based (see {@code config/SecurityConfig}): we never use the
 * default in-memory user, so suppressing it also removes the noisy generated-password
 * log line. This is unrelated to the (now removed) temporary security-disable hack —
 * the JWT {@code SecurityFilterChain} remains active.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
