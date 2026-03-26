package edu.ruc.platform.worklog.repository;

import edu.ruc.platform.worklog.domain.StudentWorkLogActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentWorkLogActionLogRepository extends JpaRepository<StudentWorkLogActionLog, Long> {

    List<StudentWorkLogActionLog> findByWorkLogIdOrderByCreatedAtAsc(Long workLogId);
}
