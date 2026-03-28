package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.LatestWorkflowNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestWorkflowNodeRepository extends JpaRepository<LatestWorkflowNode, Long> {

    List<LatestWorkflowNode> findByWfIdAndIsDeletedOrderBySeqNoAsc(Long wfId, Integer isDeleted);
}
