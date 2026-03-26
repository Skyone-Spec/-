package edu.ruc.platform.platform.dto;

import java.util.List;

public record PlatformStudentDataScopeResponse(
        Long userId,
        String username,
        String role,
        boolean allAccess,
        boolean selfOnly,
        String grade,
        List<String> classNames,
        List<Long> studentIds
) {
}
