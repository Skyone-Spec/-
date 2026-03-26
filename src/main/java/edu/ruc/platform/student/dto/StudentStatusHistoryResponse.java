package edu.ruc.platform.student.dto;

import java.time.LocalDateTime;

public record StudentStatusHistoryResponse(
        Long id,
        Long studentId,
        String fromStatus,
        String toStatus,
        String changedToMajor,
        String reason,
        String changedBy,
        String changedByRole,
        LocalDateTime changedAt
) {
}
