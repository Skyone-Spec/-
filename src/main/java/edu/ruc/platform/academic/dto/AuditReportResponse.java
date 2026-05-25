package edu.ruc.platform.academic.dto;

import java.util.List;

public record AuditReportResponse(
        Long studentId,
        String studentName,
        String programName,
        Integer totalRequiredCredits,
        Integer earnedCredits,
        Integer missingCredits,
        Double completionRate,
        List<MissingModuleResponse> missingModules,
        List<String> recommendations
) {
    public record MissingModuleResponse(
            String moduleName,
            String moduleType,
            Integer requiredCredits,
            Integer earnedCredits,
            Integer missingCredits
    ) {}
}
