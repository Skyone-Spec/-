package edu.ruc.platform.knowledge.service;

import edu.ruc.platform.knowledge.dto.KnowledgeDetailResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;

import java.util.List;

public interface KnowledgeApplicationService {

    List<KnowledgeSearchResponse> search(String keyword);

    List<KnowledgeSearchResponse> listTemplates();

    KnowledgeDetailResponse getDetail(Long id);

    List<KnowledgeSearchResponse> recommendForStudent(Long studentId);
}
