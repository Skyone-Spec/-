package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdvisorScopeBindingUpsertRequest(
        @NotBlank(message = "班主任账号不能为空") String advisorUsername,
        String advisorName,
        String grade,
        String className,
        @NotNull(message = "学生ID不能为空") Long studentId
) {
}
