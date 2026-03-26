package edu.ruc.platform.platform.dto;

import java.time.LocalDateTime;

public record PlatformLoginAuditLogResponse(
        Long id,
        Long userId,
        String username,
        String role,
        String action,
        String result,
        String detail,
        LocalDateTime operatedAt
) {
}
