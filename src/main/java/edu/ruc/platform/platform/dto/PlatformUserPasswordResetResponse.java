package edu.ruc.platform.platform.dto;

import java.time.LocalDateTime;

public record PlatformUserPasswordResetResponse(
        Long userId,
        String username,
        String temporaryPassword,
        LocalDateTime resetAt
) {
}
