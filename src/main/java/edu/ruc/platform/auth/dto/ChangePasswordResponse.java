package edu.ruc.platform.auth.dto;

import java.time.LocalDateTime;

public record ChangePasswordResponse(
        Long userId,
        String username,
        boolean passwordResetRequired,
        LocalDateTime changedAt
) {
}
