package edu.ruc.platform.party.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

public record PartyQuizSubmitRequest(
        @NotNull(message = "题库ID不能为空")
        @Positive(message = "题库ID必须大于0")
        Long bankId,
        @NotNull(message = "答案不能为空")
        Map<Long, String> answers
) {}
