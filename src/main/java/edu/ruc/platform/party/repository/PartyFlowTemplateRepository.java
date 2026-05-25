package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.PartyFlowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartyFlowTemplateRepository extends JpaRepository<PartyFlowTemplate, Long> {
    List<PartyFlowTemplate> findByIsActiveTrue();
    Optional<PartyFlowTemplate> findByFlowCode(String flowCode);
}
