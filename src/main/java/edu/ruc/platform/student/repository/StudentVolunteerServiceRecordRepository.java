package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentVolunteerServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentVolunteerServiceRecordRepository extends JpaRepository<StudentVolunteerServiceRecord, Long> {
    List<StudentVolunteerServiceRecord> findByStudentIdOrderByUpdatedAtDescIdDesc(Long studentId);
    Optional<StudentVolunteerServiceRecord> findByIdAndStudentId(Long id, Long studentId);
}
