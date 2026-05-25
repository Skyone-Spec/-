package edu.ruc.platform.certificate.dto;

import java.time.LocalDateTime;

public record CertificateTemplateResponse(
        Long id,
        String templateCode,
        String templateName,
        String certificateType,
        String templateContent,
        String templateFilePath,
        String outputFormat,
        Boolean isActive,
        String description,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
