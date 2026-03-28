package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.LatestPartyReminderTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestPartyReminderTaskRepository extends JpaRepository<LatestPartyReminderTask, Long> {

    List<LatestPartyReminderTask> findByProgressIdOrderByDueAtAsc(Long progressId);
}
