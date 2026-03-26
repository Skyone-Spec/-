package edu.ruc.platform.admin.dto;

public record AdminStatsResponse(
        int totalStudents,
        int pendingApprovals,
        int publishedNotices,
        int knowledgeDocuments
) {
}
