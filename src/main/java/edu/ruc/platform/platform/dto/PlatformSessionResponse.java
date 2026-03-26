package edu.ruc.platform.platform.dto;

import java.time.LocalDateTime;

public record PlatformSessionResponse(
        Long id,
        Long userId,
        String username,
        String role,
        LocalDateTime loginAt,
        LocalDateTime logoutAt,
        boolean active
) {
}
