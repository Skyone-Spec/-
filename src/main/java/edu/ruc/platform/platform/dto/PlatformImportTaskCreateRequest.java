package edu.ruc.platform.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlatformImportTaskCreateRequest(
        @NotBlank(message = "任务类型不能为空") String taskType,
        @NotBlank(message = "文件名不能为空") String fileName,
        @NotNull(message = "总行数不能为空") @Min(value = 0, message = "总行数不能为负数") Integer totalRows
) {
}
