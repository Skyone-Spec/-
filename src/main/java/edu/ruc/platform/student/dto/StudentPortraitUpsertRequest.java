package edu.ruc.platform.student.dto;

public record StudentPortraitUpsertRequest(
        String gender,
        String ethnicity,
        String honors,
        String scholarships,
        String competitions,
        String socialPractice,
        String volunteerService,
        String researchExperience,
        String disciplineRecords,
        String dailyPerformance,
        Double gpa,
        Integer gradeRank,
        Integer majorRank,
        Integer creditsEarned,
        String careerOrientation,
        String remarks,
        String updatedBy,
        String dataSource,
        Boolean publicVisible
) {
}
