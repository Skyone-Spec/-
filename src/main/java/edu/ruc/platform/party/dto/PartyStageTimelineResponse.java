package edu.ruc.platform.party.dto;

import java.time.LocalDate;
import java.util.List;

public record PartyStageTimelineResponse(
        Long studentId,
        String currentStage,
        List<StageNode> stages
) {
    public record StageNode(String stageName,
                            boolean completed,
                            boolean current,
                            LocalDate reachedDate,
                            LocalDate expectedDeadline,
                            Integer expectedDurationDays,
                            String reminderRule,
                            String description) {
    }
}
