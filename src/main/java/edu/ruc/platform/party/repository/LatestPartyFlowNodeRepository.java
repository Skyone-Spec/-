package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.LatestPartyFlowNode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LatestPartyFlowNodeRepository extends JpaRepository<LatestPartyFlowNode, Long> {
}
