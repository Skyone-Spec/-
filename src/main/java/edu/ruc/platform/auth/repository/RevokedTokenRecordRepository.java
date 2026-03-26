package edu.ruc.platform.auth.repository;

import edu.ruc.platform.auth.domain.RevokedTokenRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RevokedTokenRecordRepository extends JpaRepository<RevokedTokenRecord, Long> {

    Optional<RevokedTokenRecord> findByTokenHash(String tokenHash);
}
