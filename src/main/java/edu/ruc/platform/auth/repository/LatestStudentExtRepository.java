package edu.ruc.platform.auth.repository;

import edu.ruc.platform.auth.domain.LatestStudentExt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LatestStudentExtRepository extends JpaRepository<LatestStudentExt, String> {

    Optional<LatestStudentExt> findByStudentNoAndIsDeleted(String studentNo, Integer isDeleted);
}
