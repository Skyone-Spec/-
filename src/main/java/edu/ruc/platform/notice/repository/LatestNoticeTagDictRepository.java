package edu.ruc.platform.notice.repository;

import edu.ruc.platform.notice.domain.LatestNoticeTagDict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestNoticeTagDictRepository extends JpaRepository<LatestNoticeTagDict, Long> {

    Optional<LatestNoticeTagDict> findByTagCode(String tagCode);
}
