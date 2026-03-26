package edu.ruc.platform.worklog.dto;

public record StudentWorkloadSummaryResponse(
        Long studentId,
        String studentName,
        Integer totalEntries,
        Integer totalWorkloadScore
) {
}
