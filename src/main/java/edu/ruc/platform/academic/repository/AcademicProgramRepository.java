package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.AcademicProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicProgramRepository extends JpaRepository<AcademicProgram, Long> {
    List<AcademicProgram> findByMajorAndGrade(String major, String grade);
    List<AcademicProgram> findByIsActiveTrue();
    Optional<AcademicProgram> findByProgramCode(String programCode);
}
