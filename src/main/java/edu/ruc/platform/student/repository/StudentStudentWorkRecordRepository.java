package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentStudentWorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentStudentWorkRecordRepository extends JpaRepository<StudentStudentWorkRecord, Long> {
    List<StudentStudentWorkRecord> findByStudentIdOrderByUpdatedAtDescIdDesc(Long studentId);
    Optional<StudentStudentWorkRecord> findByIdAndStudentId(Long id, Long studentId);
}
