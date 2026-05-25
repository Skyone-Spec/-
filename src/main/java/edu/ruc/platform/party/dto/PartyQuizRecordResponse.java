package edu.ruc.platform.party.dto;

import java.time.LocalDateTime;

public record PartyQuizRecordResponse(
        Long recordId,
        Long bankId,
        String bankName,
        Integer totalQuestions,
        Integer correctCount,
        Integer score,
        Integer totalScore,
        Boolean passed,
        LocalDateTime completedAt
) {}
