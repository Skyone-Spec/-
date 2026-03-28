package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.LatestWorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestWorkflowDefinitionRepository extends JpaRepository<LatestWorkflowDefinition, Long> {

    Optional<LatestWorkflowDefinition> findFirstByBusinessTypeAndIsActiveAndIsDeleted(String businessType, Integer isActive, Integer isDeleted);
}
