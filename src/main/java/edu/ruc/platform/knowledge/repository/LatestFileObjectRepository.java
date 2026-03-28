package edu.ruc.platform.knowledge.repository;

import edu.ruc.platform.knowledge.domain.LatestFileObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestFileObjectRepository extends JpaRepository<LatestFileObject, Long> {

    List<LatestFileObject> findByPurposeAndIsDeleted(String purpose, Integer isDeleted);
}
