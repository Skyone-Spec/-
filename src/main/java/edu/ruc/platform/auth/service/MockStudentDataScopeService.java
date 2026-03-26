package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.dto.StudentDataScopeSnapshot;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.mock.MockDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockStudentDataScopeService {

    private final MockDataStore mockDataStore;

    public void requireStudentAccess(AuthenticatedUser user, Long studentId) {
        if (!canAccessStudent(user, studentId)) {
            throw new BusinessException("学生不存在或无权访问");
        }
    }

    public boolean canAccessStudent(AuthenticatedUser user, Long studentId) {
        var targetStudent = switch (studentId.intValue()) {
            case 10001 -> mockDataStore.user("2023100001");
            case 10002 -> mockDataStore.user("2023100002");
            default -> null;
        };
        if (targetStudent == null) {
            return false;
        }
        if (isAdminScope(user)) {
            return true;
        }
        if ("LEAGUE_SECRETARY".equals(user.role()) || "CLASS_LEADER".equals(user.role())) {
            return user.grade() != null && user.grade().equals(targetStudent.grade());
        }
        if ("CLASS_ADVISOR".equals(user.role())) {
            return user.grade() != null
                    && user.grade().equals(targetStudent.grade())
                    && (("advisor01".equals(user.username()) && studentId.equals(10001L))
                    || ("advisor02".equals(user.username()) && studentId.equals(10002L)));
        }
        return "STUDENT".equals(user.role()) && studentId.equals(user.userId());
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
            if (!("advisor01".equals(user.username()) && "计科一班".equals(targetClassName))
                    && !("advisor02".equals(user.username()) && "计科二班".equals(targetClassName))) {
                throw new BusinessException("无权访问该班级数据");
            }
        }
    }

    public StudentDataScopeSnapshot describeScope(AuthenticatedUser user) {
        if (isAdminScope(user)) {
            return new StudentDataScopeSnapshot(user.role(), true, false, null, java.util.List.of(), java.util.List.of());
        }
        if ("STUDENT".equals(user.role())) {
            return new StudentDataScopeSnapshot(user.role(), false, true, user.grade(), java.util.List.of(), java.util.List.of(user.userId()));
        }
        if ("LEAGUE_SECRETARY".equals(user.role()) || "CLASS_LEADER".equals(user.role())) {
            return new StudentDataScopeSnapshot(user.role(), false, false, user.grade(), java.util.List.of(), java.util.List.of());
        }
        if ("CLASS_ADVISOR".equals(user.role())) {
            if ("advisor01".equals(user.username())) {
                return new StudentDataScopeSnapshot(user.role(), false, false, user.grade(), java.util.List.of("计科一班"), java.util.List.of(10001L));
            }
            if ("advisor02".equals(user.username())) {
                return new StudentDataScopeSnapshot(user.role(), false, false, user.grade(), java.util.List.of("计科二班"), java.util.List.of(10002L));
            }
        }
        return new StudentDataScopeSnapshot(user.role(), false, false, user.grade(), java.util.List.of(), java.util.List.of());
    }

    private boolean isAdminScope(AuthenticatedUser user) {
        return "SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role());
    }
}
