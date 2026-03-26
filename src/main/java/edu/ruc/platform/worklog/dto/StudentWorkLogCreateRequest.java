package edu.ruc.platform.worklog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StudentWorkLogCreateRequest(
        @NotNull(message = "学生ID不能为空") Long studentId,
        @NotBlank(message = "学生姓名不能为空") String studentName,
        @NotBlank(message = "分类不能为空") String category,
        @NotBlank(message = "标题不能为空") String title,
        String description,
        @NotNull(message = "工作量不能为空") @Min(value = 1, message = "工作量必须大于0") Integer workloadScore,
        @NotNull(message = "工作日期不能为空") LocalDate workDate
) {
}
