package edu.ruc.platform.party.dto;

import java.util.List;

public record PartyQuestionBankResponse(
        Long id,
        String bankCode,
        String bankName,
        String category,
        Integer questionCount,
        Boolean isActive,
        String description,
        List<PartyQuestionResponse> questions
) {}
