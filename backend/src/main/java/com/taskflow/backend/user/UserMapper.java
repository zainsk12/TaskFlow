package com.taskflow.backend.user;

import com.taskflow.backend.user.dto.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Explicit mapper between the {@link User} document and its wire DTOs.
 *
 * <p>Keeping this mapping in one place is what guarantees {@code passwordHash} is
 * never serialised: {@link UserResponse} has no such field, and this is the only
 * path from a {@code User} to a response body.
 */
@Component
public class UserMapper {

    /**
     * Maps a persisted {@link User} to the safe {@link UserResponse} view.
     *
     * @param user the document (must not be {@code null})
     * @return the response DTO, deliberately without {@code passwordHash}
     */
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl(),
                user.getTimezone(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
