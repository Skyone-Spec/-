package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentInnovationEntrepreneurshipRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentInnovationEntrepreneurshipRecordRepository extends JpaRepository<StudentInnovationEntrepreneurshipRecord, Long> {
    List<StudentInnovationEntrepreneurshipRecord> findByStudentIdOrderByUpdatedAtDescIdDesc(Long studentId);
    Optional<StudentInnovationEntrepreneurshipRecord> findByIdAndStudentId(Long id, Long studentId);
}
