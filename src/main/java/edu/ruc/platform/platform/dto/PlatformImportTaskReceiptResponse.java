package edu.ruc.platform.platform.dto;

import edu.ruc.platform.admin.dto.DataImportErrorItemResponse;

import java.time.LocalDateTime;
import java.util.List;

public record PlatformImportTaskReceiptResponse(
        Long taskId,
        String taskType,
        String fileName,
        String fileType,
        String templateName,
        String templateDownloadUrl,
        String status,
        Integer totalRows,
        Integer successRows,
        Integer failedRows,
        Integer progressPercent,
        String owner,
        LocalDateTime createdAt,
        Integer errorCount,
        List<DataImportErrorItemResponse> recentErrors,
        List<String> acceptedFileExtensions,
        String nextAction,
        boolean canRetry,
        String maintenanceOwner,
        boolean currentUserCanMaintain,
        boolean ownerOnlyMaintenance,
        boolean pendingErrorResolution,
        String receiptCode,
        LocalDateTime generatedAt,
        String executionBatchNo,
        String callbackSource
) {
}
