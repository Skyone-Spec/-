package edu.ruc.platform.student.dto;

public record StudentPortraitPageItemResponse(
        Long studentId,
        String studentNo,
        String studentName,
        String grade,
        String className,
        Double gpa,
        Integer gradeRank,
        Integer majorRank,
        Integer creditsEarned,
        String careerOrientation,
        Boolean publicVisible,
        String updatedBy
) {
}
