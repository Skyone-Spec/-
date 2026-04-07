package edu.ruc.platform.platform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record PlatformImportExecutionResultRequest(
        @NotBlank(message = "执行批次号不能为空") String executionBatchNo,
        @NotBlank(message = "回调来源不能为空") String callbackSource,
        @Pattern(
                regexp = "CREATED|RUNNING|SUCCESS|PARTIAL_SUCCESS|FAILED",
                message = "导入任务状态仅支持 CREATED、RUNNING、SUCCESS、PARTIAL_SUCCESS、FAILED"
        )
        @NotBlank(message = "状态不能为空") String status,
        @NotNull(message = "成功行数不能为空") @Min(value = 0, message = "成功行数不能为负数") Integer successRows,
        @NotNull(message = "失败行数不能为空") @Min(value = 0, message = "失败行数不能为负数") Integer failedRows,
        String errorSummary,
        @Valid List<PlatformImportErrorCreateRequest> errors
) {
}
