package edu.ruc.platform.honor.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HonorShowcaseAdminResponse(
        Long id,
        Integer awardYear,
        String honorCategory,
        String recipientType,
        String title,
        String description,
        Boolean publicVisible,
        Integer displayOrder,
        LocalDateTime displayStartAt,
        LocalDateTime displayEndAt,
        Long importTaskId,
        String createdByName,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long recipientCount,
        List<HonorRecipientAdminResponse> recipients
) {
}
