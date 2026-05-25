package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.AcademicProgramModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicProgramModuleRepository extends JpaRepository<AcademicProgramModule, Long> {
    List<AcademicProgramModule> findByProgramIdOrderBySortOrder(Long programId);
    List<AcademicProgramModule> findByProgramIdAndModuleType(Long programId, String moduleType);
}
