package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.LatestAcademicAuditMissing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestAcademicAuditMissingRepository extends JpaRepository<LatestAcademicAuditMissing, Long> {

    List<LatestAcademicAuditMissing> findByReportId(Long reportId);
}
