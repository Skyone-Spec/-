package edu.ruc.platform.party.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PartyMaterialReviewRequest(
        @NotBlank(message = "审批动作不能为空")
        String action,
        String reviewComment
) {}
