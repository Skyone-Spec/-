package edu.ruc.platform.knowledge.repository;

import edu.ruc.platform.knowledge.domain.LatestCertTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestCertTemplateRepository extends JpaRepository<LatestCertTemplate, Long> {

    List<LatestCertTemplate> findByIsDeletedAndIsActive(Integer isDeleted, Integer isActive);
}
