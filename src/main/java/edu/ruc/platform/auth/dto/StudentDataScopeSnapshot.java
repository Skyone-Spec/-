package edu.ruc.platform.auth.dto;

import java.util.List;

public record StudentDataScopeSnapshot(
        String role,
        boolean allAccess,
        boolean selfOnly,
        String grade,
        List<String> classNames,
        List<Long> studentIds
) {
}
