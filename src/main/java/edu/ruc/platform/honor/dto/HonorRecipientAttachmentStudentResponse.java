package edu.ruc.platform.honor.dto;

public record HonorRecipientAttachmentStudentResponse(
        Long id,
        Long fileId,
        String attachmentType,
        String fileName,
        String contentType,
        Long fileSize,
        String storagePath,
        String caption
) {
}
