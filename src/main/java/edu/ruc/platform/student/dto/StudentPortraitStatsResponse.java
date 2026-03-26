package edu.ruc.platform.student.dto;

import java.util.List;

public record StudentPortraitStatsResponse(
        int totalPortraits,
        int publicVisibleCount,
        double averageGpa,
        List<StudentPortraitCareerStatsResponse> careerStats,
        List<StudentPortraitGpaBandStatsResponse> gpaBandStats,
        List<StudentPortraitRankBandStatsResponse> gradeRankBandStats
) {
}
