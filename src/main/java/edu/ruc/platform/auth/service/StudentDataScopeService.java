package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.dto.StudentDataScopeSnapshot;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.student.domain.AdvisorScopeBinding;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.AdvisorScopeBindingRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class StudentDataScopeService {

    private final StudentProfileRepository studentProfileRepository;
    private final AdvisorScopeBindingRepository advisorScopeBindingRepository;

    public void requireStudentAccess(AuthenticatedUser user, Long studentId) {
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在或无权访问"));
        if (!canAccessStudent(user, profile)) {
            throw new BusinessException("学生不存在或无权访问");
        }
    }

    public boolean canAccessStudent(AuthenticatedUser user, Long studentId) {
        return studentProfileRepository.findById(studentId)
                .map(profile -> canAccessStudent(user, profile))
                .orElse(false);
    }

    public boolean canAccessStudent(AuthenticatedUser user, StudentProfile profile) {
        if (user == null || profile == null) {
            return false;
        }
        if (isAdminScope(user)) {
            return true;
        }
        if ("LEAGUE_SECRETARY".equals(user.role()) || "CLASS_LEADER".equals(user.role())) {
            return user.grade() != null && user.grade().equals(profile.getGrade());
        }
        if ("CLASS_ADVISOR".equals(user.role())) {
            if (user.grade() == null || user.grade().isBlank() || !user.grade().equals(profile.getGrade())) {
                return false;
            }
            List<AdvisorScopeBinding> bindings = advisorScopeBindingRepository
                    .findByAdvisorUsernameOrAdvisorName(user.username(), user.name());
            if (!bindings.isEmpty()) {
                return bindings.stream().anyMatch(binding -> profile.getId().equals(binding.getStudentId()));
            }
            if (profile.getAdvisorScope() == null || profile.getAdvisorScope().isBlank()) {
                return true;
            }
            return profile.getAdvisorScope().contains(user.username()) || profile.getAdvisorScope().contains(user.name());
        }
        return "STUDENT".equals(user.role()) && profile.getId().equals(user.userId());
    }

    public void requireGradeAccess(AuthenticatedUser user, String targetGrade) {
        if (isAdminScope(user)) {
            return;
        }
        if (targetGrade == null || targetGrade.isBlank()) {
            throw new BusinessException("目标年级不能为空");
        }
        if (("CLASS_ADVISOR".equals(user.role()) || "LEAGUE_SECRETARY".equals(user.role()) || "CLASS_LEADER".equals(user.role()))
                && user.grade() != null
                && user.grade().equals(targetGrade)) {
            return;
        }
        throw new BusinessException("无权访问该年级数据");
    }

    public void requireStudentScope(AuthenticatedUser user, String targetGrade, String targetClassName) {
        if (isAdminScope(user)) {
            return;
        }
        if ("LEAGUE_SECRETARY".equals(user.role()) || "CLASS_LEADER".equals(user.role())) {
            if (targetGrade == null || targetGrade.isBlank() || user.grade() == null || !user.grade().equals(targetGrade)) {
                throw new BusinessException("无权访问该学生范围数据");
            }
            return;
        }
        if (!"CLASS_ADVISOR".equals(user.role())) {
            throw new BusinessException("无权访问该学生范围数据");
        }
        if (user.grade() == null || user.grade().isBlank()) {
            throw new BusinessException("当前班主任未配置负责年级");
        }
        if (targetGrade != null && !targetGrade.isBlank() && !user.grade().equals(targetGrade)) {
            throw new BusinessException("无权访问该年级数据");
        }
        if (targetClassName != null && !targetClassName.isBlank()) {
            List<AdvisorScopeBinding> bindings = advisorScopeBindingRepository
                    .findByAdvisorUsernameOrAdvisorName(user.username(), user.name());
            if (!bindings.isEmpty()) {
                boolean matchedClass = bindings.stream().anyMatch(binding -> targetClassName.equals(binding.getClassName()));
                if (!matchedClass) {
                    throw new BusinessException("无权访问该班级数据");
                }
            }
        }
    }

    public StudentDataScopeSnapshot describeScope(AuthenticatedUser user) {
        if (isAdminScope(user)) {
            return new StudentDataScopeSnapshot(user.role(), true, false, null, List.of(), List.of());
        }
        if ("STUDENT".equals(user.role())) {
            return new StudentDataScopeSnapshot(user.role(), false, true, user.grade(), List.of(), List.of(user.userId()));
        }
        if ("LEAGUE_SECRETARY".equals(user.role()) || "CLASS_LEADER".equals(user.role())) {
            return new StudentDataScopeSnapshot(user.role(), false, false, user.grade(), List.of(), List.of());
        }
        if ("CLASS_ADVISOR".equals(user.role())) {
            List<AdvisorScopeBinding> bindings = advisorScopeBindingRepository
                    .findByAdvisorUsernameOrAdvisorName(user.username(), user.name());
            List<String> classNames = bindings.stream()
                    .map(AdvisorScopeBinding::getClassName)
                    .filter(value -> value != null && !value.isBlank())
                    .distinct()
                    .sorted()
                    .toList();
            List<Long> studentIds = bindings.stream()
                    .map(AdvisorScopeBinding::getStudentId)
                    .filter(value -> value != null)
                    .distinct()
                    .sorted()
                    .toList();
            if (!bindings.isEmpty()) {
                String grade = bindings.stream()
                        .map(AdvisorScopeBinding::getGrade)
                        .filter(value -> value != null && !value.isBlank())
                        .distinct()
                        .collect(Collectors.joining(","));
                return new StudentDataScopeSnapshot(user.role(), false, false, grade.isBlank() ? user.grade() : grade, classNames, studentIds);
            }
            return new StudentDataScopeSnapshot(user.role(), false, false, user.grade(), List.of(), List.of());
        }
        return new StudentDataScopeSnapshot(user.role(), false, false, user.grade(), List.of(), List.of());
    }

    private boolean isAdminScope(AuthenticatedUser user) {
        return "SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role());
    }
}
