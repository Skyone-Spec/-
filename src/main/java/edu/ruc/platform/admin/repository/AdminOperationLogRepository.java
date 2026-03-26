package edu.ruc.platform.admin.repository;

import edu.ruc.platform.admin.domain.AdminOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLog, Long> {
}
