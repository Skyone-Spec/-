package edu.ruc.platform.auth.repository;

import edu.ruc.platform.auth.domain.UserSessionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRecordRepository extends JpaRepository<UserSessionRecord, Long> {

    Optional<UserSessionRecord> findByTokenHash(String tokenHash);
}
