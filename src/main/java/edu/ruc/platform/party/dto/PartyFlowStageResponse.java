package edu.ruc.platform.party.dto;

public record PartyFlowStageResponse(
        Long id,
        Integer seqNo,
        String stageCode,
        String stageName,
        String description,
        String requiredMaterials,
        Integer estimatedDays,
        Integer reminderDaysBefore,
        Boolean isActive
) {}
