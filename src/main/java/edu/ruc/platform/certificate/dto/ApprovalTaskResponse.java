package edu.ruc.platform.certificate.dto;

import java.time.LocalDateTime;

public record ApprovalTaskResponse(
        Long requestId,
        Long studentId,
        String studentName,
        String certificateType,
        String status,
        String reason,
        LocalDateTime submittedAt
) {
}
