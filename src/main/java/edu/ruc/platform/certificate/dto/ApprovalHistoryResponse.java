package edu.ruc.platform.certificate.dto;

import java.time.LocalDateTime;

public record ApprovalHistoryResponse(
        Long id,
        Long requestId,
        Long operatorId,
        String operatorName,
        String operatorRole,
        String action,
        String fromStatus,
        String toStatus,
        String comment,
        LocalDateTime operatedAt
) {
}
