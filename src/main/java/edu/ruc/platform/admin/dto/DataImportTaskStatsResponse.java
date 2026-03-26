package edu.ruc.platform.admin.dto;

public record DataImportTaskStatsResponse(
        int totalTasks,
        int createdTasks,
        int runningTasks,
        int successTasks,
        int partialSuccessTasks,
        int failedTasks,
        int totalRows,
        int totalSuccessRows,
        int totalFailedRows
) {
}
