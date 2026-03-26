package edu.ruc.platform.platform.dto;

import java.time.LocalDateTime;

public record PlatformFileUploadResponse(
        Long id,
        String bizType,
        Long bizId,
        String fileName,
        String contentType,
        Long fileSize,
        String storagePath,
        String uploadedBy,
        LocalDateTime uploadedAt
) {
}
