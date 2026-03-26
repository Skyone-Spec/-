package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.ApprovalActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalActionLogRepository extends JpaRepository<ApprovalActionLog, Long> {

    List<ApprovalActionLog> findByRequestIdOrderByCreatedAtAsc(Long requestId);
}
