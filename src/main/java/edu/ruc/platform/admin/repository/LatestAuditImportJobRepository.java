package edu.ruc.platform.admin.repository;

import edu.ruc.platform.admin.domain.LatestAuditImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LatestAuditImportJobRepository extends JpaRepository<LatestAuditImportJob, Long> {
}
