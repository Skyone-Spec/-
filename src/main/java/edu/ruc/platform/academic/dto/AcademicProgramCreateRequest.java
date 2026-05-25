package edu.ruc.platform.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AcademicProgramCreateRequest(
        @NotBlank(message = "培养方案编码不能为空")
        @Size(max = 100, message = "培养方案编码长度不能超过100")
        String programCode,
        @NotBlank(message = "培养方案名称不能为空")
        @Size(max = 200, message = "培养方案名称长度不能超过200")
        String programName,
        @NotBlank(message = "专业不能为空")
        String major,
        @NotBlank(message = "年级不能为空")
        String grade,
        @Positive(message = "总学分必须大于0")
        Integer totalCredits,
        String description
) {}
