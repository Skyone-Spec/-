package edu.ruc.platform.worklog.dto;

import java.util.List;

public record WorklogExportMetadataResponse(
        String defaultFileName,
        List<WorklogExportFieldResponse> fields
) {
}
