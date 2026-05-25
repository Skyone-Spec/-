package edu.ruc.platform.academic.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TranscriptUploadResponse(
        Long transcriptId,
        Long studentId,
        String term,
        Double gpa,
        Double totalCredits,
        Integer totalCourses,
        Integer passedCourses,
        Integer failedCourses,
        List<TranscriptItemResponse> items,
        LocalDateTime parsedAt
) {
    public record TranscriptItemResponse(
            String courseCode,
            String courseName,
            Double credits,
            String gradeText,
            Double gradePoint,
            Boolean passed
    ) {}
}
