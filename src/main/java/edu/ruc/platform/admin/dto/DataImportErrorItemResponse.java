package edu.ruc.platform.admin.dto;

import java.time.LocalDateTime;

public record DataImportErrorItemResponse(
        Long id,
        Long taskId,
        Integer rowNumber,
        String fieldName,
        String errorMessage,
        String rawValue,
        LocalDateTime createdAt
) {
}
