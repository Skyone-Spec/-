package edu.ruc.platform.admin.repository;

import edu.ruc.platform.admin.domain.LatestSysOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LatestSysOperationLogRepository extends JpaRepository<LatestSysOperationLog, Long> {
}
