package edu.ruc.platform.knowledge.repository;

import edu.ruc.platform.knowledge.domain.LatestKnowledgePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestKnowledgePolicyRepository extends JpaRepository<LatestKnowledgePolicy, Long> {

    List<LatestKnowledgePolicy> findByIsDeletedAndIsPublished(Integer isDeleted, Integer isPublished);

    List<LatestKnowledgePolicy> findByIsDeletedOrderByUpdatedAtDesc(Integer isDeleted);

    long countByIsDeletedAndIsPublished(Integer isDeleted, Integer isPublished);
}
