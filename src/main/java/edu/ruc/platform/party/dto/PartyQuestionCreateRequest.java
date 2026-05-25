package edu.ruc.platform.party.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PartyQuestionCreateRequest(
        @Positive(message = "序号必须大于0")
        Integer seqNo,
        @NotBlank(message = "题目内容不能为空")
        @Size(max = 2000, message = "题目内容长度不能超过2000")
        String questionText,
        @NotBlank(message = "选项不能为空")
        @Size(max = 2000, message = "选项长度不能超过2000")
        String options,
        @NotBlank(message = "正确答案不能为空")
        @Size(max = 10, message = "答案长度不能超过10")
        String correctAnswer,
        @Size(max = 1000, message = "解析长度不能超过1000")
        String explanation,
        Integer score
) {}
