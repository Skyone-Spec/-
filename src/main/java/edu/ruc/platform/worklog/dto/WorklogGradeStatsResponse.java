package edu.ruc.platform.worklog.dto;

public record WorklogGradeStatsResponse(
        String grade,
        Integer entryCount,
        Integer totalWorkloadScore
) {
}
