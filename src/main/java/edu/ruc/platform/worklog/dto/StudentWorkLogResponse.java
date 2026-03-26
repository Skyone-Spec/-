package edu.ruc.platform.worklog.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentWorkLogResponse(
        Long id,
        Long studentId,
        String studentName,
        String category,
        String title,
        String description,
        Integer workloadScore,
        LocalDate workDate,
        String recorderName,
        String recorderRole,
        LocalDateTime createdAt
) {
}
