package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.PartyFlowStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyFlowStageRepository extends JpaRepository<PartyFlowStage, Long> {
    List<PartyFlowStage> findByFlowIdOrderBySeqNo(Long flowId);
    List<PartyFlowStage> findByFlowIdAndIsActiveTrueOrderBySeqNo(Long flowId);
}
