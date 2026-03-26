package edu.ruc.platform.admin.dto;

import java.util.List;

public record AdvisorScopeStatsResponse(
        int totalBindings,
        int totalAdvisors,
        int totalGrades,
        int totalClasses,
        List<AdvisorScopeAdvisorStatsResponse> advisorStats
) {
}
