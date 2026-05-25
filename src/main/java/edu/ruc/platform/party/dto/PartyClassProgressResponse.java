package edu.ruc.platform.party.dto;

import java.util.List;

public record PartyClassProgressResponse(
        Long studentId,
        String studentName,
        String studentNo,
        String flowType,
        String currentStage,
        String status,
        Integer pendingMaterialCount,
        Integer totalMaterialCount
) {}
