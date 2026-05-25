package edu.ruc.platform.platform.dto;

import java.util.List;

public record BatchImportResultResponse(
        int totalRows,
        int successRows,
        int failedRows,
        List<ImportErrorItem> errors
) {
    public record ImportErrorItem(
            int rowNumber,
            String fieldName,
            String errorMessage,
            String rawValue
    ) {}
}
