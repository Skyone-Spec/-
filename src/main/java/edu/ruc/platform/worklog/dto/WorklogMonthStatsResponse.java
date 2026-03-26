package edu.ruc.platform.worklog.dto;

public record WorklogMonthStatsResponse(
        String month,
        Integer entryCount,
        Integer totalWorkloadScore
) {
}
