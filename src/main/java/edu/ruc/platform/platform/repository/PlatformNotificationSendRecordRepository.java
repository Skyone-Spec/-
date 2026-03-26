package edu.ruc.platform.platform.repository;

import edu.ruc.platform.platform.domain.PlatformNotificationSendRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformNotificationSendRecordRepository extends JpaRepository<PlatformNotificationSendRecord, Long> {

    List<PlatformNotificationSendRecord> findAllByOrderBySentAtDescCreatedAtDesc();
}
