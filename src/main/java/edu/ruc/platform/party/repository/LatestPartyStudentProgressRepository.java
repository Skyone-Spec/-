package edu.ruc.platform.party.repository;

import edu.ruc.platform.party.domain.LatestPartyStudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestPartyStudentProgressRepository extends JpaRepository<LatestPartyStudentProgress, Long> {

    Optional<LatestPartyStudentProgress> findByStudentUserIdAndIsDeleted(Long studentUserId, Integer isDeleted);
}
