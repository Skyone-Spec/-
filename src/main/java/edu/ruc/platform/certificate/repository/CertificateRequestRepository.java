package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {

    List<CertificateRequest> findByStudentId(Long studentId);

    List<CertificateRequest> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);
}
