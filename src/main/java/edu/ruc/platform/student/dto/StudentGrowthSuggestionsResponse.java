package edu.ruc.platform.student.dto;

import java.util.List;

public record StudentGrowthSuggestionsResponse(
        Long studentId,
        String studentName,
        List<StudentGrowthSuggestionItemResponse> suggestions
) {
}
