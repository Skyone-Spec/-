package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.PartyQuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartyQuestionBankRepository extends JpaRepository<PartyQuestionBank, Long> {
    List<PartyQuestionBank> findByIsActiveTrue();
    Optional<PartyQuestionBank> findByBankCode(String bankCode);
}
