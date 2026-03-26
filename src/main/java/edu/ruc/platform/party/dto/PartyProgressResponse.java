package edu.ruc.platform.party.dto;

import java.time.LocalDate;

public record PartyProgressResponse(String currentStage,
                                    LocalDate stageStartDate,
                                    Integer stageDurationDays,
                                    LocalDate nextDeadline,
                                    String completedActions,
                                    String nextAction,
                                    String nextActionRule) {
}
