package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.LatestAcademicAuditReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestAcademicAuditReportRepository extends JpaRepository<LatestAcademicAuditReport, Long> {

    Optional<LatestAcademicAuditReport> findFirstByStudentUserIdOrderByGeneratedAtDesc(Long studentUserId);
}
