package edu.ruc.platform.auth.dto;

public record AuthenticatedUser(
        Long userId,
        String username,
        String role,
        String studentNo,
        String name,
        String major,
        String grade
) {
    public UserProfileResponse toProfileResponse() {
        return new UserProfileResponse(userId, username, role, studentNo, name, major, grade);
    }
}
