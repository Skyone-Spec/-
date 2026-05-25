package edu.ruc.platform.honor.dto;

public record HonorRecipientMemberResponse(
        Long id,
        Long recipientId,
        Long studentId,
        String studentNo,
        String studentName,
        String major,
        String grade,
        String className,
        String memberRole,
        Integer displayOrder
) {
}
