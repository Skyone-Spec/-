package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentCompetitionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentCompetitionRecordRepository extends JpaRepository<StudentCompetitionRecord, Long> {
    List<StudentCompetitionRecord> findByStudentIdOrderByUpdatedAtDescIdDesc(Long studentId);
    Optional<StudentCompetitionRecord> findByIdAndStudentId(Long id, Long studentId);
}
