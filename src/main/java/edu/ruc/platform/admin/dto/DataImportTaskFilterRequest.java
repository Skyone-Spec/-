package edu.ruc.platform.admin.dto;

public record DataImportTaskFilterRequest(
        String taskType,
        String status,
        String ownerKeyword
) {
}
