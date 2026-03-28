package edu.ruc.platform.auth.repository;

import edu.ruc.platform.auth.domain.LatestUserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestUserAuthRepository extends JpaRepository<LatestUserAuth, Long> {

    Optional<LatestUserAuth> findByStudentNoAndLoginMethodAndIsDeleted(String studentNo, String loginMethod, Integer isDeleted);
}
