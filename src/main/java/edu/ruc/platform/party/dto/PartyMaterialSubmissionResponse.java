package edu.ruc.platform.party.dto;

import java.time.LocalDateTime;

public record PartyMaterialSubmissionResponse(
        Long id,
        Long studentId,
        String flowType,
        String stageName,
        String materialType,
        String title,
        String content,
        String attachmentPath,
        String status,
        String reviewComment,
        Long reviewerId,
        String reviewerName,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt
) {}
