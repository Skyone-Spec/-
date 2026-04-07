package edu.ruc.platform.academic.dto;

import java.util.List;

public record AcademicAnalysisResponse(
        Long studentId,
        String studentName,
        String major,
        String grade,
        List<AcademicWarningResponse> missingModules,
        List<String> recommendedCourses,
        Integer totalRequiredCredits,
        Integer totalEarnedCredits,
        Integer totalMissingCredits,
        Integer completionRate,
        List<String> highlightedModules,
        String summary,
        AcademicRiskSummaryResponse riskSummary,
        List<String> reviewHints,
        String dataSourceNote
) {
}
