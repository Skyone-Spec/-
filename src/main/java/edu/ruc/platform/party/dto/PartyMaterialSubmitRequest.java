package edu.ruc.platform.party.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PartyMaterialSubmitRequest(
        @NotNull(message = "学生ID不能为空")
        @Positive(message = "学生ID必须大于0")
        Long studentId,
        @NotBlank(message = "流程类型不能为空")
        String flowType,
        @NotBlank(message = "阶段名称不能为空")
        String stageName,
        @NotBlank(message = "材料类型不能为空")
        String materialType,
        String title,
        String content,
        String attachmentPath
) {}
