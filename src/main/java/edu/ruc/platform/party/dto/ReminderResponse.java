package edu.ruc.platform.party.dto;

import java.time.LocalDate;

public record ReminderResponse(
        String title,
        String content,
        LocalDate remindDate,
        String level,
        String stageName,
        String triggerRule,
        Integer daysRemaining,
        boolean overdue
) {
}
