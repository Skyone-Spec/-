package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.PartyMaterialSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PartyMaterialSubmissionRepository extends JpaRepository<PartyMaterialSubmission, Long> {
    List<PartyMaterialSubmission> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<PartyMaterialSubmission> findByStatusInAndStudentIdIn(List<String> statuses, List<Long> studentIds);
    List<PartyMaterialSubmission> findByFlowTypeAndStageName(String flowType, String stageName);
    List<PartyMaterialSubmission> findByStatusIn(List<String> statuses);
    List<PartyMaterialSubmission> findByStatusInAndWithdrawalDeadlineAfter(List<String> statuses, LocalDateTime deadline);
    long countByStatus(String status);
}
