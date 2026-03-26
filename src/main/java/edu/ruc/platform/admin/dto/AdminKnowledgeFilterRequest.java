package edu.ruc.platform.admin.dto;

public record AdminKnowledgeFilterRequest(
        String keyword,
        String category,
        Boolean published
) {
}
