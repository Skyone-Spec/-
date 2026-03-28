package edu.ruc.platform.notice.repository;

import edu.ruc.platform.notice.domain.LatestNoticeDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestNoticeDeliveryRepository extends JpaRepository<LatestNoticeDelivery, Long> {

    List<LatestNoticeDelivery> findByNoticeId(Long noticeId);
}
