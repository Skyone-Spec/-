package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.LatestAcademicProgramModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestAcademicProgramModuleRepository extends JpaRepository<LatestAcademicProgramModule, Long> {

    List<LatestAcademicProgramModule> findByProgramIdAndIsDeleted(Long programId, Integer isDeleted);
}
