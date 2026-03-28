package edu.ruc.platform.notice.repository;

import edu.ruc.platform.notice.domain.LatestNoticeItemTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestNoticeItemTagRepository extends JpaRepository<LatestNoticeItemTag, Long> {

    List<LatestNoticeItemTag> findByNoticeId(Long noticeId);
}
