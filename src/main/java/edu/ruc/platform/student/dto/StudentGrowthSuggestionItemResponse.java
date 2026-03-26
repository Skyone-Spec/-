package edu.ruc.platform.student.dto;

public record StudentGrowthSuggestionItemResponse(
        String category,
        String title,
        String description,
        String priority,
        String actionLabel,
        String actionPath
) {
}
