package edu.ruc.platform.admin.dto;

public record AdminKnowledgeItemResponse(
        Long id,
        String title,
        String category,
        String tags,
        Integer version,
        boolean published,
        String officialUrl,
        String sourceFileName,
        String audienceScope,
        String updatedBy
) {
}
