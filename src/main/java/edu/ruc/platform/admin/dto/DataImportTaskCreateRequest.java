package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DataImportTaskCreateRequest(
        @NotBlank(message = "任务类型不能为空") String taskType,
        @NotBlank(message = "文件名不能为空") String fileName,
        @NotBlank(message = "负责人不能为空") String owner,
        @NotNull(message = "总行数不能为空") @Min(value = 0, message = "总行数不能为负数") Integer totalRows
) {
}
