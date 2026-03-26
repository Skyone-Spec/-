package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record AdminOperationLogResponse(
        Long id,
        Long operatorId,
        String operatorName,
        String operatorRole,
        String module,
        String action,
        String target,
        String result,
        String detail,
        LocalDateTime operatedAt
) {
}
