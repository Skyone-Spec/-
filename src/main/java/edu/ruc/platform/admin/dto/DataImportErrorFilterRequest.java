package edu.ruc.platform.admin.dto;

public record DataImportErrorFilterRequest(
        Integer rowNumber,
        String fieldName,
        String keyword
) {
}
