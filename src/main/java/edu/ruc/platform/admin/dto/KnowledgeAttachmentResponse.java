package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record KnowledgeAttachmentResponse(
        Long id,
        Long knowledgeId,
        String fileName,
        String contentType,
        Long fileSize,
        String storagePath,
        String uploadedBy,
        LocalDateTime uploadedAt
) {
}
