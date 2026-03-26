package edu.ruc.platform.worklog.dto;

public record WorklogClassStatsResponse(
        String className,
        Integer entryCount,
        Integer totalWorkloadScore
) {
}
