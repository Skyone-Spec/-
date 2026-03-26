package edu.ruc.platform.admin.dto;

import java.util.List;

public record AdminOperationLogStatsResponse(
        int totalLogs,
        int successLogs,
        int failedLogs,
        int distinctModules,
        int distinctOperatorRoles,
        List<AdminOperationLogModuleStatsResponse> moduleStats,
        List<AdminOperationLogRoleStatsResponse> operatorRoleStats
) {
}
