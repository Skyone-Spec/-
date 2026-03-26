package edu.ruc.platform.worklog.dto;

public record WorklogStudentStatsResponse(
        Long studentId,
        String studentName,
        Integer entryCount,
        Integer totalWorkloadScore
) {
}
