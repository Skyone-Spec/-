package edu.ruc.platform.worklog.dto;

public record WorklogDailyTrendResponse(
        String date,
        Integer entryCount,
        Integer totalWorkloadScore
) {
}
