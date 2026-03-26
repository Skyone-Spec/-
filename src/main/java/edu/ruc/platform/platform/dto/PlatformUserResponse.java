package edu.ruc.platform.platform.dto;

public record PlatformUserResponse(
        Long userId,
        String username,
        String role,
        Boolean enabled,
        String studentNo,
        String name,
        String grade,
        String major
) {
}
