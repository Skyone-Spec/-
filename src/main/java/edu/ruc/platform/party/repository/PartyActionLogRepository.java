package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.PartyActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyActionLogRepository extends JpaRepository<PartyActionLog, Long> {
    List<PartyActionLog> findByStudentIdOrderByOperatedAtDesc(Long studentId);
    List<PartyActionLog> findByTargetTypeAndTargetIdOrderByOperatedAtDesc(String targetType, Long targetId);
    List<PartyActionLog> findByFlowTypeAndStageNameOrderByOperatedAtDesc(String flowType, String stageName);
}
