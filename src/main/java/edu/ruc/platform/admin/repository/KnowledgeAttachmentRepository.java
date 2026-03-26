package edu.ruc.platform.admin.repository;

import edu.ruc.platform.admin.domain.KnowledgeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeAttachmentRepository extends JpaRepository<KnowledgeAttachment, Long> {

    List<KnowledgeAttachment> findByKnowledgeIdOrderByCreatedAtDesc(Long knowledgeId);
}
