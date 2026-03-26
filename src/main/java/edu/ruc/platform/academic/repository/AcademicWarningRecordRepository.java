package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.AcademicWarningRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicWarningRecordRepository extends JpaRepository<AcademicWarningRecord, Long> {

    List<AcademicWarningRecord> findByStudentId(Long studentId);
}
