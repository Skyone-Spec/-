package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.PartyProgressRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartyProgressRecordRepository extends JpaRepository<PartyProgressRecord, Long> {

    Optional<PartyProgressRecord> findByStudentId(Long studentId);
}
