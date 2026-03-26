package edu.ruc.platform.admin.dto;

public record AdvisorScopeFilterRequest(
        String advisorUsername,
        String grade,
        String className
) {
}
