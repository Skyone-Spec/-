package edu.ruc.platform.worklog.dto;

import java.time.LocalDateTime;

public record WorklogActionLogResponse(
        Long id,
        Long workLogId,
        String operatorName,
        String operatorRole,
        String action,
        String detail,
        LocalDateTime operatedAt
) {
}
