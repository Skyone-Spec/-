package edu.ruc.platform.honor.repository;

import edu.ruc.platform.honor.domain.HonorRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HonorRecipientRepository extends JpaRepository<HonorRecipient, Long> {

    List<HonorRecipient> findByShowcaseIdOrderByDisplayOrderAscCreatedAtDesc(Long showcaseId);

    long countByShowcaseId(Long showcaseId);

    void deleteByShowcaseId(Long showcaseId);
}
