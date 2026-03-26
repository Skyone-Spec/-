package edu.ruc.platform.auth.dto;

import java.time.LocalDateTime;

public record LogoutResponse(
        Long userId,
        String username,
        LocalDateTime loggedOutAt
) {
}
