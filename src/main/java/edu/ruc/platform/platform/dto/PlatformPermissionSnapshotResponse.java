package edu.ruc.platform.platform.dto;

import java.util.List;

public record PlatformPermissionSnapshotResponse(
        Long userId,
        String username,
        String role,
        String studentNo,
        String grade,
        List<String> dataScopes,
        List<String> permissionEntries
) {
}
