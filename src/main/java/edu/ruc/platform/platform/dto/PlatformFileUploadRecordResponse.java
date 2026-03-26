package edu.ruc.platform.platform.dto;

import java.time.LocalDateTime;

public record PlatformFileUploadRecordResponse(
        Long id,
        String bizType,
        Long bizId,
        String fileName,
        String contentType,
        Long fileSize,
        String storagePath,
        Long uploadedById,
        String uploadedBy,
        Boolean archived,
        Boolean deleted,
        LocalDateTime uploadedAt
) {
}
