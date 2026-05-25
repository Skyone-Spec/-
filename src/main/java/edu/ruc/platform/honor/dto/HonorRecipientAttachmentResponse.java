package edu.ruc.platform.honor.dto;

public record HonorRecipientAttachmentResponse(
        Long id,
        Long recipientId,
        Long fileId,
        String attachmentType,
        String fileName,
        String contentType,
        Long fileSize,
        String storagePath,
        String caption,
        Boolean publicVisible,
        Integer displayOrder
) {
}
