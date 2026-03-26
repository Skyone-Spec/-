package edu.ruc.platform.notice.repository;

import edu.ruc.platform.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByPublishTimeDesc();
}
