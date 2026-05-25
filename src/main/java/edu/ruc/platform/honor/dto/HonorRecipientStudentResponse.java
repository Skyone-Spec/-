package edu.ruc.platform.honor.dto;

import java.util.List;

public record HonorRecipientStudentResponse(
        Long id,
        Long showcaseId,
        String recipientType,
        String recipientName,
        String major,
        String grade,
        String className,
        String awardIntro,
        String advancedDeeds,
        Long photoFileId,
        List<HonorRecipientMemberStudentResponse> members,
        List<HonorRecipientAttachmentStudentResponse> attachments
) {
}
