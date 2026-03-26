package edu.ruc.platform.student.dto;

public record StudentDashboardFocusItemResponse(
        String type,
        String title,
        String description,
        String priority,
        String actionLabel,
        String actionPath
) {
}
