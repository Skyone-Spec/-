package edu.ruc.platform.platform.service;

import edu.ruc.platform.admin.dto.AdminOperationLogFilterRequest;
import edu.ruc.platform.admin.dto.AdminOperationLogResponse;
import edu.ruc.platform.admin.dto.DataImportErrorFilterRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemResponse;
import edu.ruc.platform.admin.dto.DataImportTaskFilterRequest;
import edu.ruc.platform.admin.dto.DataImportTaskResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.dto.StudentDataScopeSnapshot;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.auth.service.MockStudentDataScopeService;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import edu.ruc.platform.certificate.service.CertificateApplicationService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.ApprovalStatus;
import edu.ruc.platform.common.enums.DataImportTaskStatus;
import edu.ruc.platform.common.enums.DataScopeType;
import edu.ruc.platform.common.enums.NotificationChannelType;
import edu.ruc.platform.common.enums.NotificationSendStatus;
import edu.ruc.platform.common.enums.NotificationTargetType;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.enums.StudentActionType;
import edu.ruc.platform.common.enums.StudentActionPriority;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.platform.dto.PlatformContractResponse;
import edu.ruc.platform.platform.dto.PlatformFileUploadResponse;
import edu.ruc.platform.platform.dto.PlatformFileUploadRecordResponse;
import edu.ruc.platform.platform.dto.PlatformImportErrorCreateRequest;
import edu.ruc.platform.platform.dto.PlatformImportExecutionResultRequest;
import edu.ruc.platform.platform.dto.PlatformImportTaskCreateRequest;
import edu.ruc.platform.platform.dto.PlatformImportTaskReceiptResponse;
import edu.ruc.platform.platform.dto.PlatformImportTaskUpdateRequest;
import edu.ruc.platform.platform.dto.PlatformLoginAuditLogResponse;
import edu.ruc.platform.platform.dto.PlatformNotificationSendRecordResponse;
import edu.ruc.platform.platform.dto.PlatformNotificationSendRequest;
import edu.ruc.platform.platform.dto.PlatformPermissionSnapshotResponse;
import edu.ruc.platform.platform.dto.PlatformRoleResponse;
import edu.ruc.platform.platform.dto.PlatformSecurityPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformSecurityPolicyUpdateRequest;
import edu.ruc.platform.platform.dto.PlatformSessionResponse;
import edu.ruc.platform.platform.dto.PlatformStudentDataScopeResponse;
import edu.ruc.platform.platform.dto.PlatformStudentScopeCheckResponse;
import edu.ruc.platform.platform.dto.PlatformStudentQueryResponse;
import edu.ruc.platform.platform.dto.PlatformStudentUiContractResponse;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyUpdateRequest;
import edu.ruc.platform.platform.dto.PlatformUserDetailResponse;
import edu.ruc.platform.platform.dto.PlatformUserPasswordResetRequest;
import edu.ruc.platform.platform.dto.PlatformUserPasswordResetResponse;
import edu.ruc.platform.platform.dto.PlatformUserResponse;
import edu.ruc.platform.platform.dto.PlatformUserRoleStatsResponse;
import edu.ruc.platform.platform.dto.PlatformUserStatsResponse;
import edu.ruc.platform.platform.dto.PlatformUserUpsertRequest;
import edu.ruc.platform.platform.support.StudentActionPathRegistry;
import edu.ruc.platform.platform.support.StudentUiMetaRegistry;
import edu.ruc.platform.student.dto.StudentProfileFilterRequest;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockPlatformService implements PlatformApplicationService {

    private final CurrentUserService currentUserService;
    private final StudentProfileApplicationService studentProfileService;
    private final AdminApplicationService adminService;
    private final CertificateApplicationService certificateService;
    private final PlatformSecurityPolicyService platformSecurityPolicyService;
    private final PlatformUploadPolicyService platformUploadPolicyService;
    private final PlatformNotificationSendRecordService platformNotificationSendRecordService;
    private final MockStudentDataScopeService mockStudentDataScopeService;
    private final AtomicLong uploadIdGenerator = new AtomicLong(1000);
    private final AtomicLong userIdGenerator = new AtomicLong(30000);
    private final AtomicLong loginAuditIdGenerator = new AtomicLong(5000);
    private final AtomicLong sessionIdGenerator = new AtomicLong(8000);
    private final Map<String, String> userPasswords = new ConcurrentHashMap<>();
    private final List<PlatformFileUploadRecordResponse> uploadRecords = new ArrayList<>();
    private final List<PlatformLoginAuditLogResponse> loginAuditLogs = new ArrayList<>(List.of(
            new PlatformLoginAuditLogResponse(5001L, 1L, "admin", "SUPER_ADMIN", "LOGIN", "SUCCESS", null, LocalDateTime.of(2026, 3, 24, 9, 0)),
            new PlatformLoginAuditLogResponse(5002L, 10001L, "2023100001", "STUDENT", "LOGOUT", "SUCCESS", null, LocalDateTime.of(2026, 3, 24, 10, 0))
    ));
    private final List<PlatformSessionResponse> sessions = new ArrayList<>(List.of(
            new PlatformSessionResponse(8001L, 1L, "admin", "SUPER_ADMIN", LocalDateTime.of(2026, 3, 24, 9, 0), null, true),
            new PlatformSessionResponse(8002L, 10001L, "2023100001", "STUDENT", LocalDateTime.of(2026, 3, 24, 9, 30), LocalDateTime.of(2026, 3, 24, 10, 0), false)
    ));
    private final List<PlatformUserDetailResponse> users = new ArrayList<>(List.of(
            new PlatformUserDetailResponse(1L, "admin", RoleType.SUPER_ADMIN.name(), true, null, "系统管理员", null, null, false, false, 0, false, LocalDateTime.of(2026, 3, 20, 9, 0), LocalDateTime.of(2026, 3, 20, 9, 0)),
            new PlatformUserDetailResponse(20001L, "teacher01", RoleType.COUNSELOR.name(), true, null, "胡浩老师", null, null, false, false, 0, false, LocalDateTime.of(2026, 3, 20, 9, 5), LocalDateTime.of(2026, 3, 20, 9, 5)),
            new PlatformUserDetailResponse(20002L, "advisor01", RoleType.CLASS_ADVISOR.name(), true, null, "王老师", "2023级", null, false, false, 0, false, LocalDateTime.of(2026, 3, 20, 9, 10), LocalDateTime.of(2026, 3, 20, 9, 10)),
            new PlatformUserDetailResponse(10002L, "2023100002", RoleType.LEAGUE_SECRETARY.name(), true, "2023100002", "李四", "2023级", "计算机类", true, false, 0, false, LocalDateTime.of(2026, 3, 20, 9, 15), LocalDateTime.of(2026, 3, 20, 9, 15)),
            new PlatformUserDetailResponse(10001L, "2023100001", RoleType.STUDENT.name(), true, "2023100001", "张三", "2023级", "计算机类", true, false, 0, false, LocalDateTime.of(2026, 3, 20, 9, 20), LocalDateTime.of(2026, 3, 20, 9, 20))
    ));

    @Override
    public PlatformContractResponse contract() {
        return new PlatformContractResponse(
                "userId",
                "studentId",
                "studentNo",
                "CurrentUserService",
                "PlatformFileUploadResponse",
                "page,size",
                "ApiResponse<PageResponse<T>>",
                enumNames(RoleType.values()),
                enumNames(DataScopeType.values()),
                enumNames(ApprovalStatus.values()),
                enumNames(DataImportTaskStatus.values()),
                enumNames(NotificationChannelType.values()),
                enumNames(NotificationTargetType.values()),
                enumNames(NotificationSendStatus.values()),
                List.of("operatorId", "operatorName", "operatorRole", "action", "fromStatus", "toStatus", "comment", "operatedAt"),
                List.of("operatorId", "operatorName", "operatorRole", "module", "action", "target", "result", "detail"),
                enumNames(StudentActionType.values()),
                enumNames(StudentActionPriority.values()),
                StudentActionPathRegistry.PATHS
        );
    }

    @Override
    public PlatformStudentUiContractResponse studentUiContract() {
        return new PlatformStudentUiContractResponse(
                enumNames(StudentActionType.values()),
                enumNames(StudentActionPriority.values()),
                StudentUiMetaRegistry.all()
        );
    }

    @Override
    public PlatformUploadPolicyResponse getUploadPolicy() {
        return platformUploadPolicyService.getPolicy();
    }

    @Override
    public PlatformUploadPolicyResponse updateUploadPolicy(PlatformUploadPolicyUpdateRequest request) {
        return platformUploadPolicyService.updatePolicy(request);
    }

    @Override
    public PlatformPermissionSnapshotResponse currentPermissions() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return new PlatformPermissionSnapshotResponse(
                user.userId(),
                user.username(),
                user.role(),
                user.studentNo(),
                user.grade(),
                resolveDataScopes(user),
                List.of("requireCurrentUser", "requireAnyRole", "requireSelfOrAdmin", "requireStudentAccess", "requireStudentScopeOrAdmin")
        );
    }

    @Override
    public PlatformStudentDataScopeResponse currentStudentDataScope() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentDataScopeSnapshot snapshot = mockStudentDataScopeService.describeScope(user);
        return new PlatformStudentDataScopeResponse(
                user.userId(),
                user.username(),
                user.role(),
                snapshot.allAccess(),
                snapshot.selfOnly(),
                snapshot.grade(),
                snapshot.classNames(),
                snapshot.studentIds()
        );
    }

    @Override
    public PlatformStudentScopeCheckResponse checkStudentAccess(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        try {
            mockStudentDataScopeService.requireStudentAccess(user, studentId);
            return new PlatformStudentScopeCheckResponse(true, "STUDENT", studentId, null, null, null);
        } catch (BusinessException ex) {
            return new PlatformStudentScopeCheckResponse(false, "STUDENT", studentId, null, null, ex.getMessage());
        }
    }

    @Override
    public PlatformStudentScopeCheckResponse checkStudentScope(String grade, String className) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        try {
            mockStudentDataScopeService.requireStudentScope(user, grade, className);
            return new PlatformStudentScopeCheckResponse(true, "RANGE", null, grade, className, null);
        } catch (BusinessException ex) {
            return new PlatformStudentScopeCheckResponse(false, "RANGE", null, grade, className, ex.getMessage());
        }
    }

    @Override
    public List<PlatformRoleResponse> listRoles() {
        return List.of(
                new PlatformRoleResponse(RoleType.SUPER_ADMIN.name(), "超级管理员", List.of(DataScopeType.ALL.name())),
                new PlatformRoleResponse(RoleType.COLLEGE_ADMIN.name(), "学院管理员", List.of(DataScopeType.ALL.name())),
                new PlatformRoleResponse(RoleType.COUNSELOR.name(), "辅导员", List.of(DataScopeType.ALL.name())),
                new PlatformRoleResponse(RoleType.CLASS_ADVISOR.name(), "班主任", List.of(DataScopeType.GRADE.name(), DataScopeType.CLASS.name())),
                new PlatformRoleResponse(RoleType.CLASS_LEADER.name(), "班长", List.of(DataScopeType.GRADE.name(), DataScopeType.SELF.name())),
                new PlatformRoleResponse(RoleType.LEAGUE_SECRETARY.name(), "团支书", List.of(DataScopeType.GRADE.name(), DataScopeType.SELF.name())),
                new PlatformRoleResponse(RoleType.STUDENT.name(), "普通学生", List.of(DataScopeType.SELF.name())),
                new PlatformRoleResponse(RoleType.ASSISTANT.name(), "学生助理", List.of(DataScopeType.SELF.name()))
        );
    }

    @Override
    public PlatformSecurityPolicyResponse getSecurityPolicy() {
        return platformSecurityPolicyService.getPolicy();
    }

    @Override
    public PlatformSecurityPolicyResponse updateSecurityPolicy(PlatformSecurityPolicyUpdateRequest request) {
        return platformSecurityPolicyService.updatePolicy(request);
    }

    @Override
    public List<PlatformUserResponse> listUsers(String role, Boolean enabled, String keyword) {
        String normalizedRole = validateUserRoleFilter(role);
        String normalizedKeyword = QueryFilterSupport.trimToNull(keyword);
        return users.stream()
                .map(this::toUserResponse)
                .filter(item -> normalizedRole == null || normalizedRole.equals(item.role()))
                .filter(item -> enabled == null || enabled.equals(item.enabled()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.username(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.name(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.studentNo(), normalizedKeyword))
                .toList();
    }

    @Override
    public PlatformUserDetailResponse getUser(Long userId) {
        return users.stream()
                .filter(item -> item.userId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("平台用户不存在"));
    }

    @Override
    public PlatformUserDetailResponse createUser(PlatformUserUpsertRequest request) {
        boolean exists = users.stream().anyMatch(item -> item.username().equals(request.username()));
        if (exists) {
            throw new BusinessException("用户名已存在");
        }
        RoleType roleType = parseRole(request.role());
        validateUserUpsertRequest(request, true);
        PlatformUserDetailResponse created = new PlatformUserDetailResponse(
                userIdGenerator.incrementAndGet(),
                request.username(),
                roleType.name(),
                request.enabled() == null || request.enabled(),
                request.username().matches("\\d{8,}") ? request.username() : null,
                request.username(),
                null,
                null,
                false,
                request.passwordResetRequired() == null
                        ? platformSecurityPolicyService.requirePasswordResetOnCreate()
                        : request.passwordResetRequired(),
                0,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        userPasswords.put(created.username(), resolvePassword(request.rawPassword()));
        users.add(0, created);
        return created;
    }

    @Override
    public PlatformUserDetailResponse updateUser(Long userId, PlatformUserUpsertRequest request) {
        RoleType roleType = parseRole(request.role());
        validateUserUpsertRequest(request, false);
        for (int i = 0; i < users.size(); i++) {
            PlatformUserDetailResponse item = users.get(i);
            if (item.userId().equals(userId)) {
                PlatformUserDetailResponse updated = new PlatformUserDetailResponse(
                        item.userId(),
                        request.username(),
                        roleType.name(),
                        request.enabled() == null ? item.enabled() : request.enabled(),
                        request.username().matches("\\d{8,}") ? request.username() : item.studentNo(),
                        item.name(),
                        item.grade(),
                        item.major(),
                        item.wechatBound(),
                        request.passwordResetRequired() == null ? item.passwordResetRequired() : request.passwordResetRequired(),
                        item.failedLoginAttempts(),
                        item.locked(),
                        item.createdAt(),
                        LocalDateTime.now()
                );
                users.set(i, updated);
                transferStoredPassword(item.username(), updated.username());
                if (request.rawPassword() != null && !request.rawPassword().isBlank()) {
                    userPasswords.put(updated.username(), request.rawPassword());
                }
                if (!Boolean.TRUE.equals(updated.enabled())) {
                    deactivateSessionsByUserId(updated.userId());
                }
                return updated;
            }
        }
        throw new BusinessException("平台用户不存在");
    }

    @Override
    public PlatformUserDetailResponse changeUserEnabled(Long userId, Boolean enabled) {
        for (int i = 0; i < users.size(); i++) {
            PlatformUserDetailResponse item = users.get(i);
            if (item.userId().equals(userId)) {
                PlatformUserDetailResponse updated = new PlatformUserDetailResponse(
                        item.userId(),
                        item.username(),
                        item.role(),
                        enabled,
                        item.studentNo(),
                        item.name(),
                        item.grade(),
                        item.major(),
                        item.wechatBound(),
                        item.passwordResetRequired(),
                        item.failedLoginAttempts(),
                        item.locked(),
                        item.createdAt(),
                        LocalDateTime.now()
                );
                users.set(i, updated);
                if (!Boolean.TRUE.equals(enabled)) {
                    deactivateSessionsByUserId(updated.userId());
                }
                return updated;
            }
        }
        throw new BusinessException("平台用户不存在");
    }

    @Override
    public PlatformUserDetailResponse unlockUser(Long userId) {
        for (int i = 0; i < users.size(); i++) {
            PlatformUserDetailResponse item = users.get(i);
            if (item.userId().equals(userId)) {
                PlatformUserDetailResponse updated = new PlatformUserDetailResponse(
                        item.userId(),
                        item.username(),
                        item.role(),
                        item.enabled(),
                        item.studentNo(),
                        item.name(),
                        item.grade(),
                        item.major(),
                        item.wechatBound(),
                        item.passwordResetRequired(),
                        0,
                        false,
                        item.createdAt(),
                        LocalDateTime.now()
                );
                users.set(i, updated);
                return updated;
            }
        }
        throw new BusinessException("平台用户不存在");
    }

    @Override
    public PlatformUserPasswordResetResponse resetPassword(Long userId, PlatformUserPasswordResetRequest request) {
        PlatformUserDetailResponse user = getUser(userId);
        String temporaryPassword = request == null || request.newPassword() == null || request.newPassword().isBlank()
                ? platformSecurityPolicyService.defaultPassword()
                : request.newPassword();
        validatePassword(temporaryPassword, "重置密码长度不能少于 6 位");
        for (int i = 0; i < users.size(); i++) {
            PlatformUserDetailResponse item = users.get(i);
            if (item.userId().equals(userId)) {
                users.set(i, new PlatformUserDetailResponse(
                        item.userId(),
                        item.username(),
                        item.role(),
                        item.enabled(),
                        item.studentNo(),
                        item.name(),
                        item.grade(),
                        item.major(),
                        item.wechatBound(),
                        true,
                        item.failedLoginAttempts(),
                        item.locked(),
                        item.createdAt(),
                        LocalDateTime.now()
                ));
                userPasswords.put(item.username(), temporaryPassword);
                break;
            }
        }
        deactivateSessionsByUserId(userId);
        return new PlatformUserPasswordResetResponse(user.userId(), user.username(), temporaryPassword, LocalDateTime.now());
    }

    @Override
    public PageResponse<PlatformUserResponse> pageUsers(String role, Boolean enabled, String keyword, int page, int size) {
        List<PlatformUserResponse> filtered = listUsers(role, enabled, keyword);
        return toPage(filtered, page, size);
    }

    @Override
    public PlatformUserStatsResponse userStats(String role, Boolean enabled, String keyword) {
        validateUserRoleFilter(role);
        List<PlatformUserResponse> filtered = listUsers(role, enabled, keyword);
        List<PlatformUserRoleStatsResponse> roleStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(PlatformUserResponse::role))
                .entrySet()
                .stream()
                .map(entry -> new PlatformUserRoleStatsResponse(entry.getKey(), entry.getValue().size()))
                .sorted(java.util.Comparator.comparing(PlatformUserRoleStatsResponse::role))
                .toList();
        return new PlatformUserStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> Boolean.TRUE.equals(item.enabled())).count(),
                (int) filtered.stream().filter(item -> !Boolean.TRUE.equals(item.enabled())).count(),
                (int) filtered.stream().filter(item -> RoleType.STUDENT.name().equals(item.role()) || RoleType.LEAGUE_SECRETARY.name().equals(item.role())).count(),
                (int) filtered.stream().filter(item -> !RoleType.STUDENT.name().equals(item.role()) && !RoleType.LEAGUE_SECRETARY.name().equals(item.role())).count(),
                roleStats
        );
    }

    @Override
    public PageResponse<PlatformSessionResponse> pageSessions(Long userId, Boolean active, String keyword, int page, int size) {
        String normalizedKeyword = QueryFilterSupport.trimToNull(keyword);
        List<PlatformSessionResponse> filtered = sessions.stream()
                .filter(item -> userId == null || userId.equals(item.userId()))
                .filter(item -> active == null || active.equals(item.active()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.username(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.role(), normalizedKeyword))
                .toList();
        return toPage(filtered, page, size);
    }

    @Override
    public PlatformSessionResponse revokeSession(Long sessionId) {
        for (int i = 0; i < sessions.size(); i++) {
            PlatformSessionResponse item = sessions.get(i);
            if (item.id().equals(sessionId)) {
                PlatformSessionResponse updated = new PlatformSessionResponse(
                        item.id(),
                        item.userId(),
                        item.username(),
                        item.role(),
                        item.loginAt(),
                        LocalDateTime.now(),
                        false
                );
                sessions.set(i, updated);
                return updated;
            }
        }
        throw new BusinessException("会话不存在");
    }

    public PlatformUserDetailResponse findUserByUsername(String username) {
        return users.stream()
                .filter(item -> item.username().equals(username))
                .findFirst()
                .orElse(null);
    }

    public boolean passwordMatches(String username, String password) {
        PlatformUserDetailResponse user = findUserByUsername(username);
        if (user == null) {
            return false;
        }
        String storedPassword = userPasswords.get(username);
        if (storedPassword != null) {
            return storedPassword.equals(password);
        }
        return platformSecurityPolicyService.defaultPassword().equals(password);
    }

    public AuthenticatedUser buildAuthenticatedUser(String username) {
        PlatformUserDetailResponse user = users.stream()
                .filter(item -> item.username().equals(username))
                .findFirst()
                .orElseThrow(() -> new BusinessException("平台用户不存在"));
        Long studentId = resolveStudentId(user);
        return new AuthenticatedUser(
                user.userId(),
                studentId,
                user.username(),
                user.role(),
                user.studentNo(),
                user.name(),
                user.major(),
                user.grade()
        );
    }

    public void markLoginFailure(String username, int maxFailedLoginAttempts) {
        for (int i = 0; i < users.size(); i++) {
            PlatformUserDetailResponse item = users.get(i);
            if (item.username().equals(username)) {
                int failedAttempts = item.failedLoginAttempts() + 1;
                boolean locked = failedAttempts >= maxFailedLoginAttempts;
                users.set(i, new PlatformUserDetailResponse(
                        item.userId(),
                        item.username(),
                        item.role(),
                        item.enabled(),
                        item.studentNo(),
                        item.name(),
                        item.grade(),
                        item.major(),
                        item.wechatBound(),
                        item.passwordResetRequired(),
                        failedAttempts,
                        locked,
                        item.createdAt(),
                        LocalDateTime.now()
                ));
                return;
            }
        }
    }

    public void clearLoginFailures(String username) {
        for (int i = 0; i < users.size(); i++) {
            PlatformUserDetailResponse item = users.get(i);
            if (item.username().equals(username)) {
                users.set(i, new PlatformUserDetailResponse(
                        item.userId(),
                        item.username(),
                        item.role(),
                        item.enabled(),
                        item.studentNo(),
                        item.name(),
                        item.grade(),
                        item.major(),
                        item.wechatBound(),
                        item.passwordResetRequired(),
                        0,
                        false,
                        item.createdAt(),
                        LocalDateTime.now()
                ));
                return;
            }
        }
    }

    public void clearPasswordResetRequired(String username) {
        for (int i = 0; i < users.size(); i++) {
            PlatformUserDetailResponse item = users.get(i);
            if (item.username().equals(username)) {
                users.set(i, new PlatformUserDetailResponse(
                        item.userId(),
                        item.username(),
                        item.role(),
                        item.enabled(),
                        item.studentNo(),
                        item.name(),
                        item.grade(),
                        item.major(),
                        item.wechatBound(),
                        false,
                        item.failedLoginAttempts(),
                        item.locked(),
                        item.createdAt(),
                        LocalDateTime.now()
                ));
                return;
            }
        }
    }

    @Override
    public PlatformStudentQueryResponse getStudent(Long studentId) {
        return toPlatformStudent(studentProfileService.getStudent(studentId));
    }

    @Override
    public PageResponse<PlatformStudentQueryResponse> pageStudents(String grade, String className, String status, String keyword, int page, int size) {
        PageResponse<StudentProfileResponse> result = studentProfileService.pageStudents(
                new StudentProfileFilterRequest(grade, className, status, keyword),
                page,
                size
        );
        return new PageResponse<>(
                result.content().stream().map(this::toPlatformStudent).toList(),
                result.totalElements(),
                result.totalPages(),
                result.page(),
                result.size()
        );
    }

    @Override
    public PlatformFileUploadResponse upload(String bizType, Long bizId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        validateUploadByPolicy(file);
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        String normalizedBizType = (bizType == null || bizType.isBlank()) ? "COMMON" : bizType.trim().toUpperCase();
        String originalFileName = file.getOriginalFilename() == null ? "unknown-file" : file.getOriginalFilename();
        PlatformFileUploadResponse response = new PlatformFileUploadResponse(
                uploadIdGenerator.incrementAndGet(),
                normalizedBizType,
                bizId,
                originalFileName,
                file.getContentType(),
                file.getSize(),
                "/uploads/" + normalizedBizType.toLowerCase() + "/" + System.currentTimeMillis() + "-" + originalFileName,
                user.name(),
                LocalDateTime.now()
        );
        uploadRecords.add(0, new PlatformFileUploadRecordResponse(
                response.id(),
                response.bizType(),
                response.bizId(),
                response.fileName(),
                response.contentType(),
                response.fileSize(),
                response.storagePath(),
                user.userId(),
                response.uploadedBy(),
                false,
                false,
                response.uploadedAt()
        ));
        return response;
    }

    @Override
    public PageResponse<PlatformFileUploadRecordResponse> pageUploadRecords(String bizType, Long bizId, String uploaderKeyword, int page, int size) {
        String normalizedBizType = QueryFilterSupport.normalizeUpper(bizType);
        String normalizedUploaderKeyword = QueryFilterSupport.trimToNull(uploaderKeyword);
        List<PlatformFileUploadRecordResponse> filtered = uploadRecords.stream()
                .filter(item -> normalizedBizType == null || normalizedBizType.equalsIgnoreCase(item.bizType()))
                .filter(item -> bizId == null || bizId.equals(item.bizId()))
                .filter(item -> normalizedUploaderKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.uploadedBy(), normalizedUploaderKeyword))
                .filter(item -> !Boolean.TRUE.equals(item.deleted()))
                .toList();
        return toPage(filtered, page, size);
    }

    @Override
    public PlatformFileUploadRecordResponse archiveUploadRecord(Long id) {
        for (int i = 0; i < uploadRecords.size(); i++) {
            PlatformFileUploadRecordResponse item = uploadRecords.get(i);
            if (item.id().equals(id)) {
                PlatformFileUploadRecordResponse updated = new PlatformFileUploadRecordResponse(
                        item.id(),
                        item.bizType(),
                        item.bizId(),
                        item.fileName(),
                        item.contentType(),
                        item.fileSize(),
                        item.storagePath(),
                        item.uploadedById(),
                        item.uploadedBy(),
                        true,
                        item.deleted(),
                        item.uploadedAt()
                );
                uploadRecords.set(i, updated);
                return updated;
            }
        }
        throw new BusinessException("上传记录不存在");
    }

    @Override
    public void deleteUploadRecord(Long id) {
        for (int i = 0; i < uploadRecords.size(); i++) {
            PlatformFileUploadRecordResponse item = uploadRecords.get(i);
            if (item.id().equals(id)) {
                uploadRecords.set(i, new PlatformFileUploadRecordResponse(
                        item.id(),
                        item.bizType(),
                        item.bizId(),
                        item.fileName(),
                        item.contentType(),
                        item.fileSize(),
                        item.storagePath(),
                        item.uploadedById(),
                        item.uploadedBy(),
                        item.archived(),
                        true,
                        item.uploadedAt()
                ));
                return;
            }
        }
        throw new BusinessException("上传记录不存在");
    }

    @Override
    public PlatformImportTaskReceiptResponse createImportTask(PlatformImportTaskCreateRequest request) {
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        validateImportTaskCreateRequest(request.taskType(), request.fileName());
        DataImportTaskResponse task = adminService.createImportTask(new edu.ruc.platform.admin.dto.DataImportTaskCreateRequest(
                request.taskType(),
                request.fileName(),
                currentUser.name(),
                request.totalRows()
        ));
        return buildImportTaskReceipt(task);
    }

    @Override
    public PlatformImportTaskReceiptResponse updateImportTask(Long taskId, PlatformImportTaskUpdateRequest request) {
        requireImportTaskMaintenancePermission(taskId);
        validateImportTaskUpdatePayload(taskId, request);
        DataImportTaskResponse task = adminService.updateImportTask(taskId, new edu.ruc.platform.admin.dto.DataImportTaskUpdateRequest(
                request.status(),
                request.successRows(),
                request.failedRows(),
                request.errorSummary()
        ));
        return buildImportTaskReceipt(task);
    }

    @Override
    public PageResponse<DataImportTaskResponse> pageImportTasks(String taskType, String status, String ownerKeyword, int page, int size) {
        String normalizedStatus = validateImportTaskStatusFilter(status);
        return adminService.pageImportTasks(new DataImportTaskFilterRequest(taskType, normalizedStatus, ownerKeyword), page, size);
    }

    @Override
    public PlatformImportTaskReceiptResponse getImportTaskReceipt(Long taskId) {
        DataImportTaskResponse task = adminService.listImportTasks().stream()
                .filter(item -> item.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        return buildImportTaskReceipt(task);
    }

    @Override
    public PlatformImportTaskReceiptResponse createImportError(Long taskId, PlatformImportErrorCreateRequest request) {
        requireImportTaskMaintenancePermission(taskId);
        adminService.createImportError(taskId, new edu.ruc.platform.admin.dto.DataImportErrorItemCreateRequest(
                request.rowNumber(),
                request.fieldName(),
                request.errorMessage(),
                request.rawValue()
        ));
        return getImportTaskReceipt(taskId);
    }

    @Override
    public PlatformImportTaskReceiptResponse applyImportExecutionResult(Long taskId, PlatformImportExecutionResultRequest request) {
        requireImportExecutionPermission(taskId);
        validateImportExecutionErrors(taskId, request.errors());
        validateImportTaskUpdatePayload(taskId, new PlatformImportTaskUpdateRequest(
                request.status(),
                request.successRows(),
                request.failedRows(),
                request.errorSummary()
        ));
        adminService.updateImportTask(taskId, new edu.ruc.platform.admin.dto.DataImportTaskUpdateRequest(
                request.status(),
                request.successRows(),
                request.failedRows(),
                request.errorSummary()
        ));
        adminService.recordImportExecutionContext(taskId, request.executionBatchNo(), request.callbackSource());
        adminService.replaceImportErrors(taskId, request.errors() == null ? List.of() : request.errors().stream()
                .map(item -> new edu.ruc.platform.admin.dto.DataImportErrorItemCreateRequest(
                        item.rowNumber(),
                        item.fieldName(),
                        item.errorMessage(),
                        item.rawValue()
                ))
                .toList());
        return getImportTaskReceipt(taskId);
    }

    private void requireImportTaskMaintenancePermission(Long taskId) {
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        DataImportTaskResponse task = adminService.listImportTasks().stream()
                .filter(item -> item.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        if (!canMaintainImportTask(currentUser, task)) {
            throw new BusinessException("当前账号仅可维护本人创建的导入任务");
        }
    }

    private void requireImportExecutionPermission(Long taskId) {
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        DataImportTaskResponse task = adminService.listImportTasks().stream()
                .filter(item -> item.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        if (!canMaintainImportTask(currentUser, task)) {
            throw new BusinessException("导入执行结果仅允许任务负责人或学院管理员回填");
        }
    }

    @Override
    public PageResponse<DataImportErrorItemResponse> pageImportErrors(Long taskId, Integer rowNumber, String fieldName, String keyword, int page, int size) {
        return adminService.pageImportErrors(taskId, new DataImportErrorFilterRequest(rowNumber, fieldName, keyword), page, size);
    }

    @Override
    public PageResponse<AdminOperationLogResponse> pageAdminOperationLogs(String module, String action, String operatorRole, String targetKeyword, int page, int size) {
        return adminService.pageOperationLogs(new AdminOperationLogFilterRequest(module, action, operatorRole, targetKeyword), page, size);
    }

    @Override
    public PageResponse<PlatformLoginAuditLogResponse> pageLoginAuditLogs(String action, String result, String keyword, int page, int size) {
        String normalizedAction = QueryFilterSupport.normalizeUpper(action);
        String normalizedResult = QueryFilterSupport.normalizeUpper(result);
        String normalizedKeyword = QueryFilterSupport.trimToNull(keyword);
        List<PlatformLoginAuditLogResponse> filtered = loginAuditLogs.stream()
                .filter(item -> normalizedAction == null || normalizedAction.equalsIgnoreCase(item.action()))
                .filter(item -> normalizedResult == null || normalizedResult.equalsIgnoreCase(item.result()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.username(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.detail(), normalizedKeyword))
                .toList();
        return toPage(filtered, page, size);
    }

    @Override
    public List<ApprovalHistoryResponse> approvalHistory(Long requestId) {
        return certificateService.listApprovalHistory(requestId);
    }

    @Override
    public PlatformNotificationSendRecordResponse sendNotification(PlatformNotificationSendRequest request) {
        AuthenticatedUser operator = currentUserService.requireCurrentUser();
        validateNotificationRequest(request);
        return platformNotificationSendRecordService.recordSend(
                request.title(),
                request.channel(),
                request.targetType(),
                request.targetDescription(),
                request.status(),
                request.recipientCount(),
                operator.name(),
                request.sentAt(),
                request.extensionChannels()
        );
    }

    @Override
    public List<PlatformNotificationSendRecordResponse> listNotificationSendRecords() {
        return platformNotificationSendRecordService.listRecords();
    }

    @Override
    public PageResponse<PlatformNotificationSendRecordResponse> pageNotificationSendRecords(String channel, String status, String targetKeyword, int page, int size) {
        String normalizedChannel = validateNotificationChannelFilter(channel);
        String normalizedStatus = validateNotificationStatusFilter(status);
        return platformNotificationSendRecordService.pageRecords(normalizedChannel, normalizedStatus, targetKeyword, page, size);
    }

    private PlatformStudentQueryResponse toPlatformStudent(StudentProfileResponse item) {
        return new PlatformStudentQueryResponse(
                item.id(),
                item.studentNo(),
                item.name(),
                item.major(),
                item.grade(),
                item.className(),
                item.degreeLevel(),
                item.status(),
                item.graduated(),
                item.email(),
                new PlatformStudentQueryResponse.SensitiveFields(
                        item.maskedIdCardNo(),
                        item.maskedPhone(),
                        item.maskedNativePlace(),
                        item.maskedHouseholdAddress(),
                        item.maskedSupervisor()
                )
        );
    }

    private PlatformUserResponse toUserResponse(PlatformUserDetailResponse item) {
        return new PlatformUserResponse(
                item.userId(),
                item.username(),
                item.role(),
                item.enabled(),
                item.studentNo(),
                item.name(),
                item.grade(),
                item.major()
        );
    }

    private void deactivateSessionsByUserId(Long userId) {
        for (int i = 0; i < sessions.size(); i++) {
            PlatformSessionResponse item = sessions.get(i);
            if (item.userId().equals(userId) && item.active()) {
                sessions.set(i, new PlatformSessionResponse(
                        item.id(),
                        item.userId(),
                        item.username(),
                        item.role(),
                        item.loginAt(),
                        LocalDateTime.now(),
                        false
                ));
            }
        }
    }

    private void validateUserUpsertRequest(PlatformUserUpsertRequest request, boolean creating) {
        if (!request.username().equals(request.username().trim())) {
            throw new BusinessException("用户名首尾不能包含空格");
        }
        if (request.username().contains(" ")) {
            throw new BusinessException("用户名不能包含空格");
        }
        if (creating) {
            validatePassword(resolvePassword(request.rawPassword()), "平台用户密码长度不能少于 6 位");
        } else if (request.rawPassword() != null && !request.rawPassword().isBlank()) {
            validatePassword(request.rawPassword(), "平台用户密码长度不能少于 6 位");
        }
    }

    private String resolvePassword(String rawPassword) {
        return rawPassword == null || rawPassword.isBlank() ? platformSecurityPolicyService.defaultPassword() : rawPassword;
    }

    private void validatePassword(String password, String message) {
        if (password != null && !password.isBlank() && password.length() < 6) {
            throw new BusinessException(message);
        }
    }

    private void validateImportTaskCreateRequest(String taskType, String fileName) {
        List<String> allowedTaskTypes = List.of("STUDENT_PROFILE", "KNOWLEDGE_BASE", "NOTICE", "ADVISOR_SCOPE");
        if (!allowedTaskTypes.contains(taskType)) {
            throw new BusinessException("导入任务类型仅支持 STUDENT_PROFILE、KNOWLEDGE_BASE、NOTICE、ADVISOR_SCOPE");
        }
        String lowerCaseFileName = fileName.toLowerCase(java.util.Locale.ROOT);
        if (!(lowerCaseFileName.endsWith(".xlsx") || lowerCaseFileName.endsWith(".xls") || lowerCaseFileName.endsWith(".csv"))) {
            throw new BusinessException("导入文件仅支持 xlsx、xls、csv");
        }
    }

    private void validateImportTaskUpdatePayload(Long taskId, PlatformImportTaskUpdateRequest request) {
        DataImportTaskResponse current = adminService.listImportTasks().stream()
                .filter(item -> item.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        String nextStatus = DataImportTaskStatus.from(request.status()).name();
        if (request.successRows() + request.failedRows() > current.totalRows()) {
            throw new BusinessException("成功行数与失败行数之和不能超过总行数");
        }
        if (DataImportTaskStatus.SUCCESS.name().equals(current.status()) || DataImportTaskStatus.FAILED.name().equals(current.status())) {
            throw new BusinessException("已完成的导入任务不允许再次更新状态");
        }
        if (DataImportTaskStatus.CREATED.name().equals(current.status()) && DataImportTaskStatus.CREATED.name().equals(nextStatus)
                && (request.successRows() > 0 || request.failedRows() > 0)) {
            throw new BusinessException("CREATED 状态下成功行数和失败行数必须为 0");
        }
        if (DataImportTaskStatus.SUCCESS.name().equals(nextStatus) && request.successRows() != current.totalRows()) {
            throw new BusinessException("SUCCESS 状态下成功行数必须等于总行数");
        }
        if (DataImportTaskStatus.SUCCESS.name().equals(nextStatus) && request.failedRows() != 0) {
            throw new BusinessException("SUCCESS 状态下失败行数必须为 0");
        }
        if (DataImportTaskStatus.PARTIAL_SUCCESS.name().equals(nextStatus) && (request.successRows() <= 0 || request.failedRows() <= 0)) {
            throw new BusinessException("PARTIAL_SUCCESS 状态下成功行数和失败行数都必须大于 0");
        }
        if (DataImportTaskStatus.FAILED.name().equals(nextStatus) && request.successRows() != 0) {
            throw new BusinessException("FAILED 状态下成功行数必须为 0");
        }
    }

    private void validateNotificationRequest(PlatformNotificationSendRequest request) {
        if (request.extensionChannels() == null || request.extensionChannels().isEmpty()) {
            return;
        }
        long distinctCount = request.extensionChannels().stream().distinct().count();
        if (distinctCount != request.extensionChannels().size()) {
            throw new BusinessException("扩展渠道不能重复");
        }
        if (request.extensionChannels().stream().anyMatch(channel -> channel.equals(request.channel()))) {
            throw new BusinessException("扩展渠道不能与主发送渠道重复");
        }
    }

    private RoleType parseRole(String role) {
        try {
            return RoleType.valueOf(QueryFilterSupport.normalizeUpper(role));
        } catch (Exception ex) {
            throw new BusinessException("角色不存在: " + role);
        }
    }

    private String validateUserRoleFilter(String role) {
        return QueryFilterSupport.requireEnumValue(RoleType.class, role, "角色不存在: ");
    }

    private String validateImportTaskStatusFilter(String status) {
        String normalizedStatus = QueryFilterSupport.normalizeUpper(status);
        if (normalizedStatus == null) {
            return null;
        }
        DataImportTaskStatus.from(normalizedStatus);
        return normalizedStatus;
    }

    private String validateNotificationChannelFilter(String channel) {
        return QueryFilterSupport.requireEnumValue(NotificationChannelType.class, channel, "通知渠道不支持: ");
    }

    private String validateNotificationStatusFilter(String status) {
        return QueryFilterSupport.requireEnumValue(NotificationSendStatus.class, status, "通知发送状态不支持: ");
    }

    private List<String> resolveDataScopes(AuthenticatedUser user) {
        if (RoleType.SUPER_ADMIN.name().equals(user.role())
                || RoleType.COLLEGE_ADMIN.name().equals(user.role())
                || RoleType.COUNSELOR.name().equals(user.role())) {
            return List.of(DataScopeType.ALL.name());
        }
        if (RoleType.CLASS_ADVISOR.name().equals(user.role())) {
            return List.of(DataScopeType.GRADE.name(), DataScopeType.CLASS.name());
        }
        if (RoleType.CLASS_LEADER.name().equals(user.role())
                || RoleType.LEAGUE_SECRETARY.name().equals(user.role())) {
            return List.of(DataScopeType.GRADE.name(), DataScopeType.SELF.name());
        }
        return List.of(DataScopeType.SELF.name());
    }

    private void transferStoredPassword(String oldUsername, String newUsername) {
        if (oldUsername.equals(newUsername)) {
            return;
        }
        String storedPassword = userPasswords.remove(oldUsername);
        if (storedPassword != null) {
            userPasswords.put(newUsername, storedPassword);
        }
    }

    private Long resolveStudentId(PlatformUserDetailResponse user) {
        if (!StringUtils.hasText(user.studentNo())) {
            return null;
        }
        if (RoleType.STUDENT.name().equals(user.role())
                || RoleType.LEAGUE_SECRETARY.name().equals(user.role())
                || RoleType.CLASS_LEADER.name().equals(user.role())) {
            return user.userId();
        }
        return null;
    }

    private List<String> enumNames(Enum<?>[] values) {
        return java.util.Arrays.stream(values).map(Enum::name).toList();
    }

    private <T> PageResponse<T> toPage(List<T> items, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, items.size());
        int toIndex = Math.min(fromIndex + normalizedSize, items.size());
        int totalPages = (int) Math.ceil(items.size() / (double) normalizedSize);
        return new PageResponse<>(items.subList(fromIndex, toIndex), items.size(), totalPages, normalizedPage, normalizedSize);
    }

    private PlatformImportTaskReceiptResponse buildImportTaskReceipt(DataImportTaskResponse task) {
        List<DataImportErrorItemResponse> errors = adminService.listImportErrors(task.id());
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        boolean currentUserCanMaintain = canMaintainImportTask(currentUser, task);
        boolean pendingErrorResolution = task.failedRows() > 0
                && ("PARTIAL_SUCCESS".equals(task.status()) || "FAILED".equals(task.status()));
        AdminApplicationService.ImportExecutionContext executionContext = adminService.getImportExecutionContext(task.id());
        return new PlatformImportTaskReceiptResponse(
                task.id(),
                task.taskType(),
                task.fileName(),
                task.fileType(),
                task.templateName(),
                task.templateDownloadUrl(),
                task.status(),
                task.totalRows(),
                task.successRows(),
                task.failedRows(),
                task.progressPercent(),
                task.owner(),
                task.createdAt(),
                errors.size(),
                errors.stream().limit(5).toList(),
                List.of("xlsx", "xls", "csv"),
                resolveImportTaskNextAction(task.status(), errors.size()),
                canRetryImportTask(task.status()),
                task.owner(),
                currentUserCanMaintain,
                true,
                pendingErrorResolution,
                "IMP-" + task.id() + "-" + task.createdAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
                LocalDateTime.now(),
                executionContext == null ? null : executionContext.executionBatchNo(),
                executionContext == null ? null : executionContext.callbackSource()
        );
    }

    private boolean canMaintainImportTask(AuthenticatedUser currentUser, DataImportTaskResponse task) {
        if (currentUser == null || task == null) {
            return false;
        }
        if ("SUPER_ADMIN".equals(currentUser.role()) || "COLLEGE_ADMIN".equals(currentUser.role())) {
            return true;
        }
        return task.owner() != null && task.owner().equals(currentUser.name());
    }

    private void validateImportExecutionErrors(Long taskId, List<PlatformImportErrorCreateRequest> errors) {
        if (errors == null || errors.isEmpty()) {
            return;
        }
        DataImportTaskResponse task = adminService.listImportTasks().stream()
                .filter(item -> item.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        for (PlatformImportErrorCreateRequest error : errors) {
            if (error.rowNumber() > task.totalRows()) {
                throw new BusinessException("错误行号不能超过导入任务总行数");
            }
        }
    }

    private String resolveImportTaskNextAction(String status, int errorCount) {
        return switch (status) {
            case "CREATED" -> "上传文件并启动导入";
            case "RUNNING" -> "等待导入完成并关注进度";
            case "SUCCESS" -> "导入完成，可继续核验结果";
            case "PARTIAL_SUCCESS" -> errorCount > 0 ? "下载错误明细并按模板修正后重试" : "复核部分成功结果";
            case "FAILED" -> "按模板修正文件后重新发起导入";
            default -> "查看导入结果";
        };
    }

    private boolean canRetryImportTask(String status) {
        return "PARTIAL_SUCCESS".equals(status) || "FAILED".equals(status);
    }

    private void validateUploadByPolicy(MultipartFile file) {
        if (file.getSize() > platformUploadPolicyService.maxFileSizeBytes()) {
            throw new BusinessException("上传文件大小超出平台限制");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            if (!platformUploadPolicyService.allowEmptyContentType()) {
                throw new BusinessException("上传文件类型不能为空");
            }
            return;
        }
        if (!platformUploadPolicyService.allowedContentTypes().contains(contentType)) {
            throw new BusinessException("上传文件类型不在平台允许范围内");
        }
    }
}
