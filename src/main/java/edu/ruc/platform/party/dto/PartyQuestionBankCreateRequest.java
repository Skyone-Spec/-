package edu.ruc.platform.party.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PartyQuestionBankCreateRequest(
        @NotBlank(message = "题库编码不能为空")
        @Size(max = 100, message = "题库编码长度不能超过100")
        String bankCode,
        @NotBlank(message = "题库名称不能为空")
        @Size(max = 200, message = "题库名称长度不能超过200")
        String bankName,
        @NotBlank(message = "分类不能为空")
        String category,
        String description
) {}
