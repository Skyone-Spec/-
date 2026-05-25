package edu.ruc.platform.party.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PartyQuizResultResponse(
        Long recordId,
        Long studentId,
        Long bankId,
        String bankName,
        Integer totalQuestions,
        Integer correctCount,
        Integer score,
        Integer totalScore,
        Boolean passed,
        List<PartyQuizAnswerDetail> details,
        LocalDateTime completedAt
) {
    public record PartyQuizAnswerDetail(
            Long questionId,
            String questionText,
            String yourAnswer,
            String correctAnswer,
            Boolean correct,
            Integer score,
            String explanation
    ) {}
}
