package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentSkillCertificateRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentSkillCertificateRecordRepository extends JpaRepository<StudentSkillCertificateRecord, Long> {
    List<StudentSkillCertificateRecord> findByStudentIdOrderByUpdatedAtDescIdDesc(Long studentId);
    Optional<StudentSkillCertificateRecord> findByIdAndStudentId(Long id, Long studentId);
}
