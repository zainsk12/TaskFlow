package com.taskflow.backend.security;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for {@link RevokedToken} denylist entries.
 */
@Repository
public interface RevokedTokenRepository extends MongoRepository<RevokedToken, String> {

    /** Whether a refresh token with this {@code jti} has already been revoked. */
    boolean existsByJti(String jti);
}
