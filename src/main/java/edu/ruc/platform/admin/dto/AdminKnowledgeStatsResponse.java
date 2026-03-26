package edu.ruc.platform.admin.dto;

import java.util.List;

public record AdminKnowledgeStatsResponse(
        int totalItems,
        int publishedItems,
        int unpublishedItems,
        int totalAttachments,
        List<KnowledgeCategoryStatsResponse> categoryStats
) {
}
