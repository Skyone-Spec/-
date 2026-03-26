package edu.ruc.platform.admin.dto;

public record AdvisorScopeAdvisorStatsResponse(
        String advisorUsername,
        String advisorName,
        int bindingCount,
        int studentCount
) {
}
