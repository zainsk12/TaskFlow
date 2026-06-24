package com.taskflow.backend.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight, public liveness endpoint used by the deployment platform's health
 * probe (Render). It performs no I/O — it simply confirms the web layer is up and
 * accepting requests, so it stays fast and cannot be tripped by transient DB
 * latency. {@code GET /health} is permitted in {@code SecurityConfig}.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
