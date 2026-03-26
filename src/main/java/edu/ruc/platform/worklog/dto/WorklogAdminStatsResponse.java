package edu.ruc.platform.worklog.dto;

import java.util.List;

public record WorklogAdminStatsResponse(
        Integer totalEntries,
        Integer totalStudents,
        Integer totalWorkloadScore,
        List<WorklogCategoryStatsResponse> categoryStats,
        List<WorklogStudentStatsResponse> topStudents,
        List<WorklogMonthStatsResponse> monthStats,
        List<WorklogDailyTrendResponse> dailyTrend,
        List<WorklogRecorderRoleStatsResponse> recorderRoleStats,
        List<WorklogScoreBandStatsResponse> scoreBandStats,
        List<WorklogGradeStatsResponse> gradeStats,
        List<WorklogClassStatsResponse> classStats
) {
}
