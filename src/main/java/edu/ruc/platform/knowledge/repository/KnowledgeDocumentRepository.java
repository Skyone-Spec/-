package edu.ruc.platform.knowledge.repository;

import edu.ruc.platform.knowledge.domain.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByPublishedTrueAndTitleContainingIgnoreCase(String keyword);

    long countByPublishedTrue();
}
