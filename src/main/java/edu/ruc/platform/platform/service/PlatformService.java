package edu.ruc.platform.platform.service;

import edu.ruc.platform.auth.domain.UserAccount;
import edu.ruc.platform.auth.domain.LoginAuditLog;
import edu.ruc.platform.auth.domain.RevokedTokenRecord;
import edu.ruc.platform.auth.domain.UserSessionRecord;
import edu.ruc.platform.auth.dto.StudentDataScopeSnapshot;
import edu.ruc.platform.auth.repository.UserAccountRepository;
import edu.ruc.platform.auth.repository.LoginAuditLogRepository;
import edu.ruc.platform.auth.repository.RevokedTokenRecordRepository;
import edu.ruc.platform.auth.repository.UserSessionRecordRepository;
import edu.ruc.platform.auth.service.StudentDataScopeService;
import edu.ruc.platform.admin.dto.AdminOperationLogFilterRequest;
import edu.ruc.platform.admin.dto.AdminOperationLogResponse;
import edu.ruc.platform.admin.domain.AdminOperationLog;
import edu.ruc.platform.admin.dto.DataImportErrorFilterRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemResponse;
import edu.ruc.platform.admin.dto.DataImportTaskFilterRequest;
import edu.ruc.platform.admin.dto.DataImportTaskResponse;
import edu.ruc.platform.admin.repository.AdminOperationLogRepository;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
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
import edu.ruc.platform.platform.domain.PlatformFileUploadRecord;
import edu.ruc.platform.platform.repository.PlatformFileUploadRecordRepository;
import edu.ruc.platform.platform.support.StudentActionPathRegistry;
import edu.ruc.platform.platform.support.StudentUiMetaRegistry;
import edu.ruc.platform.student.dto.StudentProfileFilterRequest;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.util.StringUtils;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class PlatformService implements PlatformApplicationService {

    private final CurrentUserService currentUserService;
    private final UserAccountRepository userAccountRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentProfileApplicationService studentProfileService;
    private final AdminApplicationService adminService;
    private final CertificateApplicationService certificateService;
    private final PasswordEncoder passwordEncoder;
    private final PlatformFileUploadRecordRepository platformFileUploadRecordRepository;
    private final LoginAuditLogRepository loginAuditLogRepository;
    private final UserSessionRecordRepository userSessionRecordRepository;
    private final RevokedTokenRecordRepository revokedTokenRecordRepository;
    private final AdminOperationLogRepository adminOperationLogRepository;
    private final PlatformSecurityPolicyService platformSecurityPolicyService;
    private final PlatformUploadPolicyService platformUploadPolicyService;
    private final PlatformNotificationSendRecordService platformNotificationSendRecordService;
    private final StudentDataScopeService studentDataScopeService;

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
        PlatformUploadPolicyResponse before = platformUploadPolicyService.getPolicy();
        PlatformUploadPolicyResponse policy = platformUploadPolicyService.updatePolicy(request);
        writePlatformOperationLog("UPLOAD_POLICY", "UPDATE", "platform-upload-policy", "SUCCESS",
                buildUploadPolicyAuditDetail(before, policy));
        return policy;
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
        StudentDataScopeSnapshot snapshot = studentDataScopeService.describeScope(user);
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
            studentDataScopeService.requireStudentAccess(user, studentId);
            return new PlatformStudentScopeCheckResponse(true, "STUDENT", studentId, null, null, null);
        } catch (BusinessException ex) {
            return new PlatformStudentScopeCheckResponse(false, "STUDENT", studentId, null, null, ex.getMessage());
        }
    }

    @Override
    public PlatformStudentScopeCheckResponse checkStudentScope(String grade, String className) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        try {
            studentDataScopeService.requireStudentScope(user, grade, className);
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
                new PlatformRoleResponse(RoleType.LEAGUE_SECRETARY.name(), "团支书", List.of(DataScopeType.GRADE.name(), DataScopeType.SELF.name())),
                new PlatformRoleResponse(RoleType.STUDENT.name(), "普通学生", List.of(DataScopeType.SELF.name()))
        );
    }

    @Override
    public PlatformSecurityPolicyResponse getSecurityPolicy() {
        return platformSecurityPolicyService.getPolicy();
    }

    @Override
    public PlatformSecurityPolicyResponse updateSecurityPolicy(PlatformSecurityPolicyUpdateRequest request) {
        PlatformSecurityPolicyResponse before = platformSecurityPolicyService.getPolicy();
        PlatformSecurityPolicyResponse policy = platformSecurityPolicyService.updatePolicy(request);
        writePlatformOperationLog("SECURITY_POLICY", "UPDATE", "platform-security-policy", "SUCCESS",
                buildSecurityPolicyAuditDetail(before, policy));
        return policy;
    }

    @Override
    public List<PlatformUserResponse> listUsers(String role, Boolean enabled, String keyword) {
        String normalizedRole = validateUserRoleFilter(role);
        String normalizedKeyword = QueryFilterSupport.trimToNull(keyword);
        return userAccountRepository.findAll().stream()
                .filter(item -> normalizedRole == null || normalizedRole.equals(item.getRole().name()))
                .filter(item -> enabled == null || enabled.equals(item.getEnabled()))
                .map(this::toPlatformUser)
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.username(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.name(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.studentNo(), normalizedKeyword))
                .toList();
    }

    @Override
    public PlatformUserDetailResponse getUser(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("平台用户不存在"));
        return toUserDetail(user);
    }

    @Override
    public PlatformUserDetailResponse createUser(PlatformUserUpsertRequest request) {
        userAccountRepository.findByUsername(request.username()).ifPresent(item -> {
            throw new BusinessException("用户名已存在");
        });
        validateUserUpsertRequest(request, true);
        UserAccount user = new UserAccount();
        user.setUsername(request.username());
        user.setRole(parseRole(request.role()));
        user.setEnabled(request.enabled() == null || request.enabled());
        user.setPasswordHash(passwordEncoder.encode(resolvePassword(request.rawPassword())));
        user.setPasswordResetRequired(request.passwordResetRequired() == null
                ? platformSecurityPolicyService.requirePasswordResetOnCreate()
                : request.passwordResetRequired());
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        PlatformUserDetailResponse response = toUserDetail(userAccountRepository.save(user));
        writePlatformOperationLog("PLATFORM_USER", "CREATE", "user#" + response.userId(), "SUCCESS", response.username());
        return response;
    }

    @Override
    public PlatformUserDetailResponse updateUser(Long userId, PlatformUserUpsertRequest request) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("平台用户不存在"));
        userAccountRepository.findByUsername(request.username())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new BusinessException("用户名已存在");
                });
        validateUserUpsertRequest(request, false);
        user.setUsername(request.username());
        user.setRole(parseRole(request.role()));
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }
        if (request.passwordResetRequired() != null) {
            user.setPasswordResetRequired(request.passwordResetRequired());
        }
        if (request.rawPassword() != null && !request.rawPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.rawPassword()));
        }
        PlatformUserDetailResponse response = toUserDetail(userAccountRepository.save(user));
        writePlatformOperationLog("PLATFORM_USER", "UPDATE", "user#" + response.userId(), "SUCCESS", response.username());
        return response;
    }

    @Override
    public PlatformUserDetailResponse changeUserEnabled(Long userId, Boolean enabled) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("平台用户不存在"));
        user.setEnabled(enabled);
        PlatformUserDetailResponse response = toUserDetail(userAccountRepository.save(user));
        writePlatformOperationLog("PLATFORM_USER", enabled ? "ENABLE" : "DISABLE", "user#" + response.userId(), "SUCCESS", response.username());
        return response;
    }

    @Override
    public PlatformUserPasswordResetResponse resetPassword(Long userId, PlatformUserPasswordResetRequest request) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("平台用户不存在"));
        String password = request == null || request.newPassword() == null || request.newPassword().isBlank()
                ? platformSecurityPolicyService.defaultPassword()
                : request.newPassword();
        validatePassword(password, "重置密码长度不能少于 6 位");
        user.setPasswordHash(passwordEncoder.encode(password));
        userAccountRepository.save(user);
        writePlatformOperationLog("PLATFORM_USER", "RESET_PASSWORD", "user#" + user.getId(), "SUCCESS", user.getUsername());
        return new PlatformUserPasswordResetResponse(user.getId(), user.getUsername(), password, LocalDateTime.now());
    }

    @Override
    public PlatformUserDetailResponse unlockUser(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("平台用户不存在"));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        PlatformUserDetailResponse response = toUserDetail(userAccountRepository.save(user));
        writePlatformOperationLog("PLATFORM_USER", "UNLOCK", "user#" + response.userId(), "SUCCESS", response.username());
        return response;
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
        List<PlatformSessionResponse> filtered = userSessionRecordRepository.findAll().stream()
                .filter(item -> userId == null || userId.equals(item.getUserId()))
                .filter(item -> active == null || active.equals(item.getActive()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.getUsername(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getRole(), normalizedKeyword))
                .sorted(java.util.Comparator.comparing(UserSessionRecord::getLoginAt).reversed())
                .map(item -> new PlatformSessionResponse(
                        item.getId(),
                        item.getUserId(),
                        item.getUsername(),
                        item.getRole(),
                        item.getLoginAt(),
                        item.getLogoutAt(),
                        Boolean.TRUE.equals(item.getActive())
                ))
                .toList();
        return toPage(filtered, page, size);
    }

    @Override
    public PlatformSessionResponse revokeSession(Long sessionId) {
        UserSessionRecord session = userSessionRecordRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));
        session.setActive(Boolean.FALSE);
        session.setLogoutAt(LocalDateTime.now());
        session = userSessionRecordRepository.save(session);
        if (revokedTokenRecordRepository.findByTokenHash(session.getTokenHash()).isEmpty()) {
            RevokedTokenRecord revoked = new RevokedTokenRecord();
            revoked.setTokenHash(session.getTokenHash());
            revoked.setUserId(session.getUserId());
            revoked.setUsername(session.getUsername());
            revoked.setRevokedAt(LocalDateTime.now());
            revokedTokenRecordRepository.save(revoked);
        }
        PlatformSessionResponse response = new PlatformSessionResponse(
                session.getId(),
                session.getUserId(),
                session.getUsername(),
                session.getRole(),
                session.getLoginAt(),
                session.getLogoutAt(),
                Boolean.TRUE.equals(session.getActive())
        );
        writePlatformOperationLog("PLATFORM_SESSION", "REVOKE", "session#" + response.id(), "SUCCESS", response.username());
        return response;
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
        PlatformFileUploadRecord record = new PlatformFileUploadRecord();
        record.setBizType(normalizedBizType);
        record.setBizId(bizId);
        record.setFileName(originalFileName);
        record.setContentType(file.getContentType());
        record.setFileSize(file.getSize());
        record.setStoragePath("/uploads/" + normalizedBizType.toLowerCase() + "/" + System.currentTimeMillis() + "-" + originalFileName);
        record.setUploadedById(user.userId());
        record.setUploadedBy(user.name());
        record.setArchived(Boolean.FALSE);
        record.setDeleted(Boolean.FALSE);
        record = platformFileUploadRecordRepository.save(record);
        PlatformFileUploadResponse response = new PlatformFileUploadResponse(
                record.getId(),
                normalizedBizType,
                bizId,
                originalFileName,
                file.getContentType(),
                file.getSize(),
                record.getStoragePath(),
                user.name(),
                record.getCreatedAt()
        );
        writePlatformOperationLog("PLATFORM_FILE", "UPLOAD", "file#" + response.id(), "SUCCESS", response.fileName());
        return response;
    }

    @Override
    public PageResponse<PlatformFileUploadRecordResponse> pageUploadRecords(String bizType, Long bizId, String uploaderKeyword, int page, int size) {
        String normalizedBizType = QueryFilterSupport.normalizeUpper(bizType);
        String normalizedUploaderKeyword = QueryFilterSupport.trimToNull(uploaderKeyword);
        List<PlatformFileUploadRecordResponse> filtered = platformFileUploadRecordRepository.findAll().stream()
                .filter(item -> normalizedBizType == null || normalizedBizType.equalsIgnoreCase(item.getBizType()))
                .filter(item -> bizId == null || bizId.equals(item.getBizId()))
                .filter(item -> normalizedUploaderKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.getUploadedBy(), normalizedUploaderKeyword))
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
                .sorted(java.util.Comparator.comparing(PlatformFileUploadRecord::getCreatedAt).reversed())
                .map(item -> new PlatformFileUploadRecordResponse(
                        item.getId(),
                        item.getBizType(),
                        item.getBizId(),
                        item.getFileName(),
                        item.getContentType(),
                        item.getFileSize(),
                        item.getStoragePath(),
                        item.getUploadedById(),
                        item.getUploadedBy(),
                        item.getArchived(),
                        item.getDeleted(),
                        item.getCreatedAt()
                ))
                .toList();
        return toPage(filtered, page, size);
    }

    @Override
    public PlatformFileUploadRecordResponse archiveUploadRecord(Long id) {
        PlatformFileUploadRecord record = platformFileUploadRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("上传记录不存在"));
        record.setArchived(Boolean.TRUE);
        record = platformFileUploadRecordRepository.save(record);
        PlatformFileUploadRecordResponse response = new PlatformFileUploadRecordResponse(
                record.getId(),
                record.getBizType(),
                record.getBizId(),
                record.getFileName(),
                record.getContentType(),
                record.getFileSize(),
                record.getStoragePath(),
                record.getUploadedById(),
                record.getUploadedBy(),
                record.getArchived(),
                record.getDeleted(),
                record.getCreatedAt()
        );
        writePlatformOperationLog("PLATFORM_FILE", "ARCHIVE", "file#" + response.id(), "SUCCESS", response.fileName());
        return response;
    }

    @Override
    public void deleteUploadRecord(Long id) {
        PlatformFileUploadRecord record = platformFileUploadRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("上传记录不存在"));
        record.setDeleted(Boolean.TRUE);
        platformFileUploadRecordRepository.save(record);
        writePlatformOperationLog("PLATFORM_FILE", "DELETE", "file#" + id, "SUCCESS", record.getFileName());
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
        writePlatformOperationLog("IMPORT_TASK", "CREATE", "task#" + task.id(), "SUCCESS", task.fileName());
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
        writePlatformOperationLog("IMPORT_TASK", "UPDATE", "task#" + task.id(), task.status(), request.errorSummary());
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
        writePlatformOperationLog("IMPORT_TASK", "ADD_ERROR", "task#" + taskId, "SUCCESS",
                "row=" + request.rowNumber() + ", field=" + request.fieldName());
        return getImportTaskReceipt(taskId);
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
        List<PlatformLoginAuditLogResponse> filtered = loginAuditLogRepository.findAll().stream()
                .filter(item -> normalizedAction == null || normalizedAction.equalsIgnoreCase(item.getAction()))
                .filter(item -> normalizedResult == null || normalizedResult.equalsIgnoreCase(item.getResult()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.getUsername(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getDetail(), normalizedKeyword))
                .sorted(java.util.Comparator.comparing(LoginAuditLog::getCreatedAt).reversed())
                .map(item -> new PlatformLoginAuditLogResponse(
                        item.getId(),
                        item.getUserId(),
                        item.getUsername(),
                        item.getRole(),
                        item.getAction(),
                        item.getResult(),
                        item.getDetail(),
                        item.getCreatedAt()
                ))
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
        PlatformNotificationSendRecordResponse response = platformNotificationSendRecordService.recordSend(
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
        writePlatformOperationLog("PLATFORM_NOTIFICATION", "SEND", "notification#" + response.id(), response.status(),
                response.channel() + " -> " + response.targetType() + " / " + response.targetDescription());
        return response;
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

    private PlatformUserResponse toPlatformUser(UserAccount userAccount) {
        var studentProfile = studentProfileRepository.findByStudentNo(userAccount.getUsername()).orElse(null);
        return new PlatformUserResponse(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getRole().name(),
                userAccount.getEnabled(),
                studentProfile == null ? null : studentProfile.getStudentNo(),
                studentProfile == null ? userAccount.getUsername() : studentProfile.getName(),
                studentProfile == null ? null : studentProfile.getGrade(),
                studentProfile == null ? null : studentProfile.getMajor()
        );
    }

    private PlatformUserDetailResponse toUserDetail(UserAccount userAccount) {
        StudentProfile studentProfile = studentProfileRepository.findByStudentNo(userAccount.getUsername()).orElse(null);
        return new PlatformUserDetailResponse(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getRole().name(),
                userAccount.getEnabled(),
                studentProfile == null ? null : studentProfile.getStudentNo(),
                studentProfile == null ? userAccount.getUsername() : studentProfile.getName(),
                studentProfile == null ? null : studentProfile.getGrade(),
                studentProfile == null ? null : studentProfile.getMajor(),
                userAccount.getWechatOpenId() != null && !userAccount.getWechatOpenId().isBlank(),
                Boolean.TRUE.equals(userAccount.getPasswordResetRequired()),
                userAccount.getFailedLoginAttempts(),
                userAccount.getLockedUntil() != null && userAccount.getLockedUntil().isAfter(LocalDateTime.now()),
                userAccount.getCreatedAt(),
                userAccount.getUpdatedAt()
        );
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
        return List.of(DataScopeType.SELF.name());
    }

    private List<String> enumNames(Enum<?>[] values) {
        return java.util.Arrays.stream(values).map(Enum::name).toList();
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

    private String validateImportTaskStatusFilter(String status) {
        String normalizedStatus = QueryFilterSupport.normalizeUpper(status);
        if (normalizedStatus == null) {
            return null;
        }
        DataImportTaskStatus.from(normalizedStatus);
        return normalizedStatus;
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

    private String validateNotificationChannelFilter(String channel) {
        return QueryFilterSupport.requireEnumValue(NotificationChannelType.class, channel, "通知渠道不支持: ");
    }

    private String validateNotificationStatusFilter(String status) {
        return QueryFilterSupport.requireEnumValue(NotificationSendStatus.class, status, "通知发送状态不支持: ");
    }

    private String buildSecurityPolicyAuditDetail(PlatformSecurityPolicyResponse before, PlatformSecurityPolicyResponse after) {
        return "before[maxFailedLoginAttempts=%d,lockDurationMinutes=%d,defaultPassword=%s,requirePasswordResetOnCreate=%s]; "
                .formatted(
                        before.maxFailedLoginAttempts(),
                        before.lockDurationMinutes(),
                        before.defaultPassword(),
                        before.requirePasswordResetOnCreate()
                )
                + "after[maxFailedLoginAttempts=%d,lockDurationMinutes=%d,defaultPassword=%s,requirePasswordResetOnCreate=%s]"
                .formatted(
                        after.maxFailedLoginAttempts(),
                        after.lockDurationMinutes(),
                        after.defaultPassword(),
                        after.requirePasswordResetOnCreate()
                );
    }

    private String buildUploadPolicyAuditDetail(PlatformUploadPolicyResponse before, PlatformUploadPolicyResponse after) {
        return "before[maxFileSizeBytes=%d,allowedContentTypes=%s,allowEmptyContentType=%s]; "
                .formatted(before.maxFileSizeBytes(), before.allowedContentTypes(), before.allowEmptyContentType())
                + "after[maxFileSizeBytes=%d,allowedContentTypes=%s,allowEmptyContentType=%s]"
                .formatted(after.maxFileSizeBytes(), after.allowedContentTypes(), after.allowEmptyContentType());
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

    private PlatformImportTaskReceiptResponse buildImportTaskReceipt(DataImportTaskResponse task) {
        List<DataImportErrorItemResponse> errors = adminService.listImportErrors(task.id());
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        boolean currentUserCanMaintain = canMaintainImportTask(currentUser, task);
        boolean pendingErrorResolution = task.failedRows() > 0
                && ("PARTIAL_SUCCESS".equals(task.status()) || "FAILED".equals(task.status()));
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
                LocalDateTime.now()
        );
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

    private boolean canMaintainImportTask(AuthenticatedUser currentUser, DataImportTaskResponse task) {
        if (currentUser == null || task == null) {
            return false;
        }
        if ("SUPER_ADMIN".equals(currentUser.role()) || "COLLEGE_ADMIN".equals(currentUser.role())) {
            return true;
        }
        return task.owner() != null && task.owner().equals(currentUser.name());
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

    private void writePlatformOperationLog(String module, String action, String target, String result, String detail) {
        AuthenticatedUser operator = currentUserService.requireCurrentUser();
        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(operator.userId());
        log.setOperatorName(operator.name());
        log.setOperatorRole(operator.role());
        log.setModule(module);
        log.setAction(action);
        log.setTarget(target);
        log.setResult(result);
        log.setDetail(detail);
        adminOperationLogRepository.save(log);
    }

    private <T> PageResponse<T> toPage(List<T> items, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, items.size());
        int toIndex = Math.min(fromIndex + normalizedSize, items.size());
        int totalPages = (int) Math.ceil(items.size() / (double) normalizedSize);
        return new PageResponse<>(items.subList(fromIndex, toIndex), items.size(), totalPages, normalizedPage, normalizedSize);
    }
}
