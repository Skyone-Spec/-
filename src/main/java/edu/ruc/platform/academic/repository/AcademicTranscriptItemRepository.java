package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.AcademicTranscriptItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicTranscriptItemRepository extends JpaRepository<AcademicTranscriptItem, Long> {
    List<AcademicTranscriptItem> findByTranscriptId(Long transcriptId);
    List<AcademicTranscriptItem> findByTranscriptIdAndPassedFalse(Long transcriptId);
}
