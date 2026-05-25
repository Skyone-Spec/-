package edu.ruc.platform.party.dto;

import java.time.LocalDateTime;

public record PartyActionLogResponse(
        Long id,
        Long studentId,
        String flowType,
        String stageName,
        String action,
        String targetType,
        Long targetId,
        Long operatorId,
        String operatorName,
        String operatorRole,
        String detail,
        LocalDateTime operatedAt
) {}
