package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.AcademicTranscript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicTranscriptRepository extends JpaRepository<AcademicTranscript, Long> {
    List<AcademicTranscript> findByStudentIdOrderByParsedAtDesc(Long studentId);
    Optional<AcademicTranscript> findFirstByStudentIdOrderByParsedAtDesc(Long studentId);
}
