package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {

    List<StudentStatusHistory> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
