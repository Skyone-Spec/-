package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.CertificateTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, Long> {
    List<CertificateTemplate> findByIsActiveTrue();
    List<CertificateTemplate> findByCertificateType(String certificateType);
    Optional<CertificateTemplate> findByTemplateCode(String templateCode);
    boolean existsByTemplateCode(String templateCode);
}
