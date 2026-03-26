package edu.ruc.platform.worklog.dto;

public record WorklogCategoryStatsResponse(
        String category,
        Integer entryCount,
        Integer totalWorkloadScore
) {
}
