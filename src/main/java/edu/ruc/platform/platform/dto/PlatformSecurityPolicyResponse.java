package edu.ruc.platform.platform.dto;

public record PlatformSecurityPolicyResponse(
        int maxFailedLoginAttempts,
        int lockDurationMinutes,
        String defaultPassword,
        boolean requirePasswordResetOnCreate
) {
}
