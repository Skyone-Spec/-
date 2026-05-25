package edu.ruc.platform.party.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PartyFlowStageCreateRequest(
        @Positive(message = "序号必须大于0")
        Integer seqNo,
        @NotBlank(message = "阶段编码不能为空")
        @Size(max = 100, message = "阶段编码长度不能超过100")
        String stageCode,
        @NotBlank(message = "阶段名称不能为空")
        @Size(max = 200, message = "阶段名称长度不能超过200")
        String stageName,
        @Size(max = 2000, message = "描述长度不能超过2000")
        String description,
        @Size(max = 1000, message = "所需材料长度不能超过1000")
        String requiredMaterials,
        Integer estimatedDays,
        Integer reminderDaysBefore
) {}
