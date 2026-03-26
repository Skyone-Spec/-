package edu.ruc.platform.academic.dto;

public record AcademicRiskSummaryResponse(
        String level,
        String message,
        boolean needsManualReview
) {
}
