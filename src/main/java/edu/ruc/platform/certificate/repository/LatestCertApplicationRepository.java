package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.LatestCertApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LatestCertApplicationRepository extends JpaRepository<LatestCertApplication, Long> {

    Optional<LatestCertApplication> findByRequestId(Long requestId);

    List<LatestCertApplication> findAllByOrderByCreatedAtDesc();
}
