package edu.ruc.platform.auth.dto;

public record UserProfileResponse(
        Long userId,
        Long studentId,
        String username,
        String role,
        String studentNo,
        String name,
        String major,
        String grade
) {
}
