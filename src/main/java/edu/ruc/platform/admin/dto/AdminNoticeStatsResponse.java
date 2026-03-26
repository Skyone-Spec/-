package edu.ruc.platform.admin.dto;

public record AdminNoticeStatsResponse(
        int totalNotices,
        int taggedNotices,
        int targetedGradeNotices,
        int targetedGraduateNotices
) {
}
