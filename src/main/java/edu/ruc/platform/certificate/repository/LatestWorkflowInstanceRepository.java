package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.LatestWorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestWorkflowInstanceRepository extends JpaRepository<LatestWorkflowInstance, Long> {

    Optional<LatestWorkflowInstance> findByBusinessTableAndBusinessId(String businessTable, Long businessId);
}
