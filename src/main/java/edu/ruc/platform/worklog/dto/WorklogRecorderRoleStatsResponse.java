package edu.ruc.platform.worklog.dto;

public record WorklogRecorderRoleStatsResponse(
        String recorderRole,
        Integer entryCount,
        Integer totalWorkloadScore
) {
}
