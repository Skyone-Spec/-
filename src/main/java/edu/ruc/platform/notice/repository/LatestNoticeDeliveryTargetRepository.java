package edu.ruc.platform.notice.repository;

import edu.ruc.platform.notice.domain.LatestNoticeDeliveryTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestNoticeDeliveryTargetRepository extends JpaRepository<LatestNoticeDeliveryTarget, Long> {

    List<LatestNoticeDeliveryTarget> findByTargetUserId(Long targetUserId);
}
