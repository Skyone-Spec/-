package edu.ruc.platform.auth.repository;

import edu.ruc.platform.auth.domain.LatestUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestUserRepository extends JpaRepository<LatestUser, Long> {

    Optional<LatestUser> findByStudentNoAndIsDeleted(String studentNo, Integer isDeleted);

    long countByIsDeleted(Integer isDeleted);
}
