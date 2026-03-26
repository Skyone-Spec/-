package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final ObjectProvider<StudentDataScopeService> studentDataScopeServiceProvider;
    private final ObjectProvider<MockStudentDataScopeService> mockStudentDataScopeServiceProvider;

    public AuthenticatedUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BusinessException("当前未登录");
        }
        return user;
    }

    public AuthenticatedUser requireAnyRole(RoleType... roles) {
        AuthenticatedUser user = requireCurrentUser();
        Set<String> allowedRoles = Set.of(roles).stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        if (!allowedRoles.contains(user.role())) {
            throw new BusinessException("当前账号无权执行该操作");
        }
        return user;
    }

    public AuthenticatedUser requireSelfOrAdmin(Long studentId, RoleType... adminRoles) {
        AuthenticatedUser user = requireCurrentUser();
        if (user.userId() != null && user.userId().equals(studentId)) {
            return user;
        }
        Set<String> allowedRoles = Set.of(adminRoles).stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        if (allowedRoles.contains(user.role())) {
            return user;
        }
        throw new BusinessException("无权查看或操作该学生数据");
    }

    public AuthenticatedUser requireStudentAccess(Long studentId, RoleType... adminRoles) {
        AuthenticatedUser user = requireCurrentUser();
        if (user.userId() != null && user.userId().equals(studentId)) {
            return user;
        }
        Set<String> allowedRoles = Set.of(adminRoles).stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        if (allowedRoles.contains(user.role())) {
            return user;
        }
        if (studentDataScopeServiceProvider.getIfAvailable() != null) {
            studentDataScopeServiceProvider.getIfAvailable().requireStudentAccess(user, studentId);
            return user;
        }
        if (mockStudentDataScopeServiceProvider.getIfAvailable() != null) {
            mockStudentDataScopeServiceProvider.getIfAvailable().requireStudentAccess(user, studentId);
            return user;
        }
        throw new BusinessException("学生不存在或无权访问");
    }

    public void requireMatchingGradeOrAdmin(String targetGrade, RoleType... adminRoles) {
        AuthenticatedUser user = requireCurrentUser();
        Set<String> allowedRoles = Set.of(adminRoles).stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        if (allowedRoles.contains(user.role())) {
            return;
        }
        if (studentDataScopeServiceProvider.getIfAvailable() != null) {
            studentDataScopeServiceProvider.getIfAvailable().requireGradeAccess(user, targetGrade);
            return;
        }
        if (mockStudentDataScopeServiceProvider.getIfAvailable() != null) {
            mockStudentDataScopeServiceProvider.getIfAvailable().requireGradeAccess(user, targetGrade);
            return;
        }
        throw new BusinessException("无权访问该年级数据");
    }

    public void requireStudentScopeOrAdmin(String targetGrade, String targetClassName, RoleType... adminRoles) {
        AuthenticatedUser user = requireCurrentUser();
        Set<String> allowedRoles = Set.of(adminRoles).stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        if (allowedRoles.contains(user.role())) {
            return;
        }
        if (studentDataScopeServiceProvider.getIfAvailable() != null) {
            studentDataScopeServiceProvider.getIfAvailable().requireStudentScope(user, targetGrade, targetClassName);
            return;
        }
        if (mockStudentDataScopeServiceProvider.getIfAvailable() != null) {
            mockStudentDataScopeServiceProvider.getIfAvailable().requireStudentScope(user, targetGrade, targetClassName);
            return;
        }
        throw new BusinessException("无权访问该学生范围数据");
    }
}
