package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByStudentNo(String studentNo);
}
