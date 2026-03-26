package edu.ruc.platform.worklog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StudentWorkLogUpdateRequest(
        @NotBlank(message = "工作分类不能为空") String category,
        @NotBlank(message = "工作标题不能为空") String title,
        String description,
        @NotNull(message = "工作量分值不能为空") @Min(value = 1, message = "工作量分值至少为 1") @Max(value = 100, message = "工作量分值不能超过 100") Integer workloadScore,
        @NotNull(message = "工作日期不能为空") LocalDate workDate
) {
}
