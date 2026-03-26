package edu.ruc.platform.admin.dto;

public record KnowledgeCategoryStatsResponse(
        String category,
        int itemCount,
        int publishedCount
) {
}
