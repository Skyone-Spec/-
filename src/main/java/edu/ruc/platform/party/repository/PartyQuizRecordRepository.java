package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.PartyQuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartyQuizRecordRepository extends JpaRepository<PartyQuizRecord, Long> {
    List<PartyQuizRecord> findByStudentIdOrderByCompletedAtDesc(Long studentId);
    Optional<PartyQuizRecord> findByStudentIdAndBankIdOrderByCompletedAtDesc(Long studentId, Long bankId);
}
