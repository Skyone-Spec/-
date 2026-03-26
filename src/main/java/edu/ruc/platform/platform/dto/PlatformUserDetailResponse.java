package edu.ruc.platform.platform.dto;

import java.time.LocalDateTime;

public record PlatformUserDetailResponse(
        Long userId,
        String username,
        String role,
        Boolean enabled,
        String studentNo,
        String name,
        String grade,
        String major,
        Boolean wechatBound,
        Boolean passwordResetRequired,
        Integer failedLoginAttempts,
        boolean locked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
