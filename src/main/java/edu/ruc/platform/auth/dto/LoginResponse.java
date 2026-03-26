package edu.ruc.platform.auth.dto;

public record LoginResponse(
        Long userId,
        String username,
        String role,
        String token,
        boolean passwordResetRequired
) {
}
