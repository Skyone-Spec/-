package edu.ruc.platform.platform.repository;

import edu.ruc.platform.platform.domain.PlatformFileUploadRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformFileUploadRecordRepository extends JpaRepository<PlatformFileUploadRecord, Long> {
}
