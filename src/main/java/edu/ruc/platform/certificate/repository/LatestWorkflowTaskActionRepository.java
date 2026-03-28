package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.LatestWorkflowTaskAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestWorkflowTaskActionRepository extends JpaRepository<LatestWorkflowTaskAction, Long> {

    List<LatestWorkflowTaskAction> findByWfTaskIdInOrderByCreatedAtAsc(List<Long> wfTaskIds);
}
