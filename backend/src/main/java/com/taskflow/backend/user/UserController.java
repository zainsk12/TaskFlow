package com.taskflow.backend.user;

import com.taskflow.backend.user.dto.ChangePasswordRequest;
import com.taskflow.backend.user.dto.UpdateProfileRequest;
import com.taskflow.backend.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the current user's profile ({@code /api/v1/users}).
 *
 * <p>Thin HTTP adapter: validates request DTOs ({@code @Valid}), delegates to
 * {@link UserService}, and maps results to {@link ResponseEntity}. The "current
 * user" is resolved inside the service from the security context — see
 * {@code docs/API_SPEC.md} §3.
 *
 * <p>TODO (future phase): {@code DELETE /users/me} — delete account with cascade.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * {@code GET /api/v1/users/me} — return the authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    /**
     * {@code PUT /api/v1/users/me} — update the authenticated user's profile.
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    /**
     * {@code PATCH /api/v1/users/me/password} — change the password.
     * Returns {@code 204 No Content} on success.
     */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
