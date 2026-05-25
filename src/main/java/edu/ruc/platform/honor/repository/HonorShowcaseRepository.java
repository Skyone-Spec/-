package edu.ruc.platform.honor.repository;

import edu.ruc.platform.honor.domain.HonorShowcase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HonorShowcaseRepository extends JpaRepository<HonorShowcase, Long> {

    List<HonorShowcase> findAllByOrderByDisplayOrderAscCreatedAtDesc();
}
