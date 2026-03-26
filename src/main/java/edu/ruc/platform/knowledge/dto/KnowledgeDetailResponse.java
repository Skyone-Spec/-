package edu.ruc.platform.knowledge.dto;

import edu.ruc.platform.admin.dto.KnowledgeAttachmentResponse;

import java.util.List;

public record KnowledgeDetailResponse(
        Long id,
        String title,
        String category,
        String officialUrl,
        String answer,
        String responseStrategy,
        boolean officialLinkOnly,
        String safetyTip,
        String sourceFileName,
        String audienceScope,
        List<KnowledgeAttachmentResponse> attachments,
        List<KnowledgeSearchResponse> relatedItems
) {
}
