package edu.ruc.platform.admin.dto;

public record AdvisorScopeBindingResponse(
        Long id,
        String advisorUsername,
        String advisorName,
        String grade,
        String className,
        Long studentId
) {
}
