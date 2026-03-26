package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record DataImportTaskResponse(
        Long id,
        String taskType,
        String fileName,
        String fileType,
        String templateName,
        String templateDownloadUrl,
        String status,
        int totalRows,
        int successRows,
        int failedRows,
        int progressPercent,
        String owner,
        LocalDateTime createdAt
) {
}
