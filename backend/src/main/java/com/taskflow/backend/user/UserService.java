package com.taskflow.backend.user;

import com.taskflow.backend.security.SecurityUtils;
import com.taskflow.backend.user.dto.ChangePasswordRequest;
import com.taskflow.backend.user.dto.UpdateProfileRequest;
import com.taskflow.backend.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Business logic for the authenticated user's own profile.
 *
 * <p>This phase implements the three "self-service" profile operations behind
 * {@code /api/v1/users/me}. All of them operate on the <em>current</em> user,
 * resolved from the JWT security context — see {@link #currentUserId()}.
 *
 * <p>Error mapping uses {@link ResponseStatusException} for now. A later phase
 * will introduce dedicated domain exceptions ({@code NotFoundException},
 * {@code ForbiddenException}) and a {@code @RestControllerAdvice} that renders
 * the {@code ApiError} body from {@code docs/API_SPEC.md} §1.3.
 *
 * <p>TODO (future phases):
 * <ul>
 *   <li>{@code deleteAccount()} — {@code DELETE /users/me}; cascade-delete the
 *       user's tasks and categories (transactional). See {@code DATABASE.md} §8.</li>
 *   <li>Admin features — e.g. {@code GET /admin/metrics} aggregations and
 *       role-restricted account management ({@code API_SPEC.md} §7).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Returns the current user's profile.
     *
     * @return the safe profile view ({@code GET /users/me})
     */
    public UserResponse getCurrentUser() {
        User user = loadCurrentUser();
        return userMapper.toResponse(user);
    }

    /**
     * Updates the current user's mutable profile fields (name, avatar, timezone).
     * Null fields in the request are left unchanged; {@code email} and {@code role}
     * are not editable here.
     *
     * @param request the partial profile update (already bean-validated)
     * @return the updated profile view ({@code PUT /users/me})
     */
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = loadCurrentUser();

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.timezone() != null) {
            user.setTimezone(request.timezone());
        }

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    /**
     * Changes the current user's password after verifying the supplied current
     * password against the stored hash.
     *
     * @param request current + new password (already bean-validated for length)
     * @throws ResponseStatusException {@code 403} if the current password is wrong
     */
    public void changePassword(ChangePasswordRequest request) {
        User user = loadCurrentUser();

        if (!passwordMatches(request.currentPassword(), user.getPasswordHash())) {
            // API_SPEC §3.3: current password incorrect -> 403 Forbidden.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current password is incorrect");
        }

        user.setPasswordHash(hashPassword(request.newPassword()));
        userRepository.save(user);
    }

    // ------------------------------------------------------------------

    /**
     * Loads the current user document, translating "not found" into a 404.
     */
    private User loadCurrentUser() {
        return userRepository.findById(currentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /**
     * Resolves the id of the authenticated principal from the JWT
     * {@code SecurityContext} (the {@code sub} claim, stored by
     * {@code JwtAuthenticationFilter}).
     */
    private String currentUserId() {
        return SecurityUtils.currentUserId();
    }

    /** Verifies a raw password against a stored BCrypt hash. */
    private boolean passwordMatches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }

    /** Hashes a raw password for storage with BCrypt. */
    private String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
