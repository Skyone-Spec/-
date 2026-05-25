package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentSocialPracticeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentSocialPracticeRecordRepository extends JpaRepository<StudentSocialPracticeRecord, Long> {
    List<StudentSocialPracticeRecord> findByStudentIdOrderByUpdatedAtDescIdDesc(Long studentId);
    Optional<StudentSocialPracticeRecord> findByIdAndStudentId(Long id, Long studentId);
}
