package edu.ruc.platform.certificate.dto;

import jakarta.validation.constraints.NotBlank;

public record ApprovalActionRequest(
        @NotBlank(message = "审批动作不能为空") String action,
        String comment
) {
}
