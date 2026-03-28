package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.LatestWorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LatestWorkflowTaskRepository extends JpaRepository<LatestWorkflowTask, Long> {

    List<LatestWorkflowTask> findByWfInstanceIdOrderByCreatedAtAsc(Long wfInstanceId);

    Optional<LatestWorkflowTask> findFirstByWfInstanceIdAndStatusOrderByCreatedAtAsc(Long wfInstanceId, String status);
}
