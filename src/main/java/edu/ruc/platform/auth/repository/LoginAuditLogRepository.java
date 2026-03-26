package edu.ruc.platform.auth.repository;

import edu.ruc.platform.auth.domain.LoginAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditLogRepository extends JpaRepository<LoginAuditLog, Long> {
}
