package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.PartyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyQuestionRepository extends JpaRepository<PartyQuestion, Long> {
    List<PartyQuestion> findByBankIdOrderBySeqNo(Long bankId);
    long countByBankId(Long bankId);
}
