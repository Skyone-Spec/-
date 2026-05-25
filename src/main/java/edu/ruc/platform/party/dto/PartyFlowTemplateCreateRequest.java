package edu.ruc.platform.party.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PartyFlowTemplateCreateRequest(
        @NotBlank(message = "流程编码不能为空")
        @Size(max = 100, message = "流程编码长度不能超过100")
        String flowCode,
        @NotBlank(message = "流程名称不能为空")
        @Size(max = 200, message = "流程名称长度不能超过200")
        String flowName,
        @NotBlank(message = "流程类型不能为空")
        String flowType,
        @Positive(message = "总阶段数必须大于0")
        Integer totalStages,
        String description
) {}
