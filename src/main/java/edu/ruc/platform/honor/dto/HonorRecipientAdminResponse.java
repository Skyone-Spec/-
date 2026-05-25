package edu.ruc.platform.honor.dto;

import java.time.LocalDateTime;
import java.util.List;

public record HonorRecipientAdminResponse(
        Long id,
        Long showcaseId,
        String recipientType,
        Long studentId,
        String studentNo,
        String recipientName,
        String major,
        String grade,
        String className,
        String awardIntro,
        String advancedDeeds,
        Long photoFileId,
        Boolean publicVisible,
        Integer displayOrder,
        LocalDateTime displayStartAt,
        LocalDateTime displayEndAt,
        Long importTaskId,
        String createdByName,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<HonorRecipientMemberResponse> members,
        List<HonorRecipientAttachmentResponse> attachments
) {
}
