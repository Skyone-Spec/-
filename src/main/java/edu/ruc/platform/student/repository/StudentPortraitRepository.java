package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentPortrait;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentPortraitRepository extends JpaRepository<StudentPortrait, Long> {

    Optional<StudentPortrait> findByStudentId(Long studentId);
}
