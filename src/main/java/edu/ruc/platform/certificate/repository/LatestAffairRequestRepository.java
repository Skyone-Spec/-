package edu.ruc.platform.certificate.repository;

import edu.ruc.platform.certificate.domain.LatestAffairRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestAffairRequestRepository extends JpaRepository<LatestAffairRequest, Long> {

    List<LatestAffairRequest> findByRequesterUserIdAndIsDeletedOrderByCreatedAtDesc(Long requesterUserId, Integer isDeleted);
}
