package edu.ruc.platform.worklog.dto;

import java.util.List;

public record WorklogOverviewResponse(
        Integer totalEntries,
        Integer totalStudents,
        Integer totalWorkloadScore,
        List<WorklogCategoryStatsResponse> categoryStats
) {
}
