package edu.ruc.platform.platform.dto;

import java.util.List;

public record PlatformUserStatsResponse(
        int totalUsers,
        int enabledUsers,
        int disabledUsers,
        int studentUsers,
        int teacherUsers,
        List<PlatformUserRoleStatsResponse> roleStats
) {
}
