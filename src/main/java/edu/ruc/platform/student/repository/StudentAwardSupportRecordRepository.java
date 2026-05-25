package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentAwardSupportRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentAwardSupportRecordRepository extends JpaRepository<StudentAwardSupportRecord, Long> {
    List<StudentAwardSupportRecord> findByStudentIdOrderByUpdatedAtDescIdDesc(Long studentId);
    Optional<StudentAwardSupportRecord> findByIdAndStudentId(Long id, Long studentId);
}
