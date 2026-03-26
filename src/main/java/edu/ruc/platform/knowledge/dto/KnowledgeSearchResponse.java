package edu.ruc.platform.knowledge.dto;

public record KnowledgeSearchResponse(Long id,
                                      String title,
                                      String category,
                                      String officialUrl,
                                      String answer,
                                      String responseStrategy,
                                      boolean officialLinkOnly) {
}
