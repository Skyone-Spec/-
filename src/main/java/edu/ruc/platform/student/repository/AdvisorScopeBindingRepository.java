package edu.ruc.platform.student.repository;

import edu.ruc.platform.student.domain.AdvisorScopeBinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvisorScopeBindingRepository extends JpaRepository<AdvisorScopeBinding, Long> {

    List<AdvisorScopeBinding> findByAdvisorUsernameOrAdvisorName(String advisorUsername, String advisorName);

    List<AdvisorScopeBinding> findByStudentId(Long studentId);
}
