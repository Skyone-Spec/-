package edu.ruc.platform.party.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PartyFlowTemplateResponse(
        Long id,
        String flowCode,
        String flowName,
        String flowType,
        Integer totalStages,
        String description,
        Boolean isActive,
        List<PartyFlowStageResponse> stages,
        LocalDateTime createdAt
) {}
