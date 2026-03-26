package edu.ruc.platform.platform.dto;

public record PlatformStudentScopeCheckResponse(
        boolean allowed,
        String checkType,
        Long studentId,
        String grade,
        String className,
        String reason
) {
}
