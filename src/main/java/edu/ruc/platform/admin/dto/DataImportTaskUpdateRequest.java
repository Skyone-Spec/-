package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DataImportTaskUpdateRequest(
        @Pattern(
                regexp = "CREATED|RUNNING|SUCCESS|PARTIAL_SUCCESS|FAILED",
                message = "导入任务状态仅支持 CREATED、RUNNING、SUCCESS、PARTIAL_SUCCESS、FAILED"
        )
        @NotBlank(message = "状态不能为空") String status,
        @NotNull(message = "成功行数不能为空") @Min(value = 0, message = "成功行数不能为负数") Integer successRows,
        @NotNull(message = "失败行数不能为空") @Min(value = 0, message = "失败行数不能为负数") Integer failedRows,
        String errorSummary
) {
}
