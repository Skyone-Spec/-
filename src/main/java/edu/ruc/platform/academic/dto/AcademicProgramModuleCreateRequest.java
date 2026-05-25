package edu.ruc.platform.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AcademicProgramModuleCreateRequest(
        @NotBlank(message = "模块编码不能为空")
        @Size(max = 100, message = "模块编码长度不能超过100")
        String moduleCode,
        @NotBlank(message = "模块名称不能为空")
        @Size(max = 200, message = "模块名称长度不能超过200")
        String moduleName,
        @NotBlank(message = "模块类型不能为空")
        String moduleType,
        @Positive(message = "要求学分必须大于0")
        Integer requiredCredits,
        String description,
        Integer sortOrder
) {}
