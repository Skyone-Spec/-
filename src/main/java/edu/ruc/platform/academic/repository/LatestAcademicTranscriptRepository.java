package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.LatestAcademicTranscript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestAcademicTranscriptRepository extends JpaRepository<LatestAcademicTranscript, Long> {

    Optional<LatestAcademicTranscript> findFirstByStudentUserIdOrderByParsedAtDesc(Long studentUserId);
}
