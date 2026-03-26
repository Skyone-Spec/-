package edu.ruc.platform.admin.dto;

public record AdminOperationLogRoleStatsResponse(
        String operatorRole,
        int count
) {
}
