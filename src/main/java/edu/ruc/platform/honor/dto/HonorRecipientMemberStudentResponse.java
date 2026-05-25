package edu.ruc.platform.honor.dto;

public record HonorRecipientMemberStudentResponse(
        Long id,
        String studentName,
        String major,
        String grade,
        String className,
        String memberRole
) {
}
