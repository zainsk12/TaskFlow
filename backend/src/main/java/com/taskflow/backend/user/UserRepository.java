package com.taskflow.backend.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for {@link User} documents.
 *
 * <p>Provides the standard CRUD contract over the {@code users} collection plus
 * the email-based lookups needed for profile management and (later) login and
 * registration.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds a user by their (lowercased, unique) email address.
     *
     * @param email the login identifier
     * @return the matching user, or empty if none exists
     */
    Optional<User> findByEmail(String email);

    /**
     * Whether an account already exists for the given email — used to reject
     * duplicate registrations before insert (in a future auth phase).
     *
     * @param email the login identifier
     * @return {@code true} if a user with this email exists
     */
    boolean existsByEmail(String email);
}
