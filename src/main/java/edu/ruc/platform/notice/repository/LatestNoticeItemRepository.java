package edu.ruc.platform.notice.repository;

import edu.ruc.platform.notice.domain.LatestNoticeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestNoticeItemRepository extends JpaRepository<LatestNoticeItem, Long> {

    List<LatestNoticeItem> findByIsDeletedOrderByPublishAtDesc(Integer isDeleted);

    long countByIsDeleted(Integer isDeleted);
}
