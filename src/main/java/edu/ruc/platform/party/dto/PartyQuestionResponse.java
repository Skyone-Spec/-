package edu.ruc.platform.party.dto;

public record PartyQuestionResponse(
        Long id,
        Integer seqNo,
        String questionText,
        String options,
        String correctAnswer,
        String explanation,
        Integer score
) {}
