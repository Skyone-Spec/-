package edu.ruc.platform.worklog.dto;

public record WorklogFilterRequest(
        Long studentId,
        String category,
        String recorderRole,
        String grade,
        String className,
        java.time.LocalDate startDate,
        java.time.LocalDate endDate
) {
}
