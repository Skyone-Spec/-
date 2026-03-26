package edu.ruc.platform.worklog.dto;

public record WorklogScoreBandStatsResponse(
        String band,
        Integer entryCount,
        Integer totalWorkloadScore
) {
}
