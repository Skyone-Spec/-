package edu.ruc.platform.worklog.repository;

import edu.ruc.platform.worklog.domain.StudentWorkLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentWorkLogRepository extends JpaRepository<StudentWorkLog, Long> {

    List<StudentWorkLog> findByStudentIdOrderByWorkDateDescCreatedAtDesc(Long studentId);
}
