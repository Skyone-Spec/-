package edu.ruc.platform.platform.controller;

import edu.ruc.platform.admin.dto.AdminOperationLogResponse;
import edu.ruc.platform.admin.dto.DataImportErrorItemResponse;
import edu.ruc.platform.admin.dto.DataImportTaskResponse;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.security.RequireRoles;
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
import edu.ruc.platform.platform.dto.PlatformUserDetailResponse;
import edu.ruc.platform.platform.dto.PlatformUserPasswordResetRequest;
import edu.ruc.platform.platform.dto.PlatformUserPasswordResetResponse;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyUpdateRequest;
import edu.ruc.platform.platform.dto.PlatformUserResponse;
import edu.ruc.platform.platform.dto.PlatformUserStatsResponse;
import edu.ruc.platform.platform.dto.PlatformUserUpsertRequest;
import edu.ruc.platform.platform.service.PlatformApplicationService;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformApplicationService platformService;

    @GetMapping("/contracts")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY, RoleType.STUDENT
    })
    public ApiResponse<PlatformContractResponse> contracts() {
        return ApiResponse.success(platformService.contract());
    }

    @GetMapping("/student-ui-contract")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY, RoleType.STUDENT
    })
    public ApiResponse<PlatformStudentUiContractResponse> studentUiContract() {
        return ApiResponse.success(platformService.studentUiContract());
    }

    @GetMapping("/users/me/permissions")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY, RoleType.STUDENT
    })
    public ApiResponse<PlatformPermissionSnapshotResponse> currentPermissions() {
        return ApiResponse.success(platformService.currentPermissions());
    }

    @GetMapping("/users/me/student-scope")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY, RoleType.STUDENT
    })
    public ApiResponse<PlatformStudentDataScopeResponse> currentStudentDataScope() {
        return ApiResponse.success(platformService.currentStudentDataScope());
    }

    @GetMapping("/users/me/student-scope/check-student")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY, RoleType.STUDENT
    })
    public ApiResponse<PlatformStudentScopeCheckResponse> checkStudentAccess(@Positive(message = "学生ID必须大于 0") @RequestParam Long studentId) {
        return ApiResponse.success(platformService.checkStudentAccess(studentId));
    }

    @GetMapping("/users/me/student-scope/check-range")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY, RoleType.STUDENT
    })
    public ApiResponse<PlatformStudentScopeCheckResponse> checkStudentScope(@RequestParam(required = false) String grade,
                                                                            @RequestParam(required = false) String className) {
        return ApiResponse.success(platformService.checkStudentScope(grade, className));
    }

    @GetMapping("/roles")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY, RoleType.STUDENT
    })
    public ApiResponse<List<PlatformRoleResponse>> roles() {
        return ApiResponse.success(platformService.listRoles());
    }

    @GetMapping("/security-policy")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformSecurityPolicyResponse> securityPolicy() {
        return ApiResponse.success(platformService.getSecurityPolicy());
    }

    @PutMapping("/security-policy")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformSecurityPolicyResponse> updateSecurityPolicy(@Valid @RequestBody PlatformSecurityPolicyUpdateRequest request) {
        return ApiResponse.success("平台安全策略已更新", platformService.updateSecurityPolicy(request));
    }

    @GetMapping("/upload-policy")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformUploadPolicyResponse> uploadPolicy() {
        return ApiResponse.success(platformService.getUploadPolicy());
    }

    @PutMapping("/upload-policy")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformUploadPolicyResponse> updateUploadPolicy(@Valid @RequestBody PlatformUploadPolicyUpdateRequest request) {
        return ApiResponse.success("平台上传策略已更新", platformService.updateUploadPolicy(request));
    }

    @GetMapping("/users")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<List<PlatformUserResponse>> listUsers(@RequestParam(required = false) String role,
                                                             @RequestParam(required = false) Boolean enabled,
                                                             @RequestParam(required = false) String keyword) {
        return ApiResponse.success(platformService.listUsers(role, enabled, keyword));
    }

    @GetMapping("/users/{userId}")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PlatformUserDetailResponse> getUser(@Positive(message = "用户ID必须大于 0") @PathVariable Long userId) {
        return ApiResponse.success(platformService.getUser(userId));
    }

    @PostMapping("/users")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformUserDetailResponse> createUser(@Valid @RequestBody PlatformUserUpsertRequest request) {
        return ApiResponse.success("平台用户已创建", platformService.createUser(request));
    }

    @PutMapping("/users/{userId}")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformUserDetailResponse> updateUser(@Positive(message = "用户ID必须大于 0") @PathVariable Long userId,
                                                              @Valid @RequestBody PlatformUserUpsertRequest request) {
        return ApiResponse.success("平台用户已更新", platformService.updateUser(userId, request));
    }

    @PostMapping("/users/{userId}/enabled")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformUserDetailResponse> changeEnabled(@Positive(message = "用户ID必须大于 0") @PathVariable Long userId,
                                                                 @RequestParam Boolean enabled) {
        return ApiResponse.success("平台用户状态已更新", platformService.changeUserEnabled(userId, enabled));
    }

    @PostMapping("/users/{userId}/unlock")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformUserDetailResponse> unlockUser(@Positive(message = "用户ID必须大于 0") @PathVariable Long userId) {
        return ApiResponse.success("平台用户已解锁", platformService.unlockUser(userId));
    }

    @PostMapping("/users/{userId}/reset-password")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformUserPasswordResetResponse> resetPassword(@Positive(message = "用户ID必须大于 0") @PathVariable Long userId,
                                                                        @RequestBody(required = false) PlatformUserPasswordResetRequest request) {
        return ApiResponse.success("平台用户密码已重置", platformService.resetPassword(userId, request));
    }

    @GetMapping("/users/page")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PageResponse<PlatformUserResponse>> pageUsers(@RequestParam(required = false) String role,
                                                                     @RequestParam(required = false) Boolean enabled,
                                                                     @RequestParam(required = false) String keyword,
                                                                     @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                     @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(platformService.pageUsers(role, enabled, keyword, page, size));
    }

    @GetMapping("/users/stats")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PlatformUserStatsResponse> userStats(@RequestParam(required = false) String role,
                                                            @RequestParam(required = false) Boolean enabled,
                                                            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(platformService.userStats(role, enabled, keyword));
    }

    @GetMapping("/sessions/page")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PageResponse<PlatformSessionResponse>> pageSessions(@Positive(message = "用户ID必须大于 0") @RequestParam(required = false) Long userId,
                                                                           @RequestParam(required = false) Boolean active,
                                                                           @RequestParam(required = false) String keyword,
                                                                           @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                           @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(platformService.pageSessions(userId, active, keyword, page, size));
    }

    @PostMapping("/sessions/{sessionId}/revoke")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PlatformSessionResponse> revokeSession(@Positive(message = "会话ID必须大于 0") @PathVariable Long sessionId) {
        return ApiResponse.success("会话已强制下线", platformService.revokeSession(sessionId));
    }

    @GetMapping("/students/{studentId}")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY, RoleType.STUDENT
    })
    public ApiResponse<PlatformStudentQueryResponse> getStudent(@Positive(message = "学生ID必须大于 0") @PathVariable Long studentId) {
        return ApiResponse.success(platformService.getStudent(studentId));
    }

    @GetMapping("/students/page")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR
    })
    public ApiResponse<PageResponse<PlatformStudentQueryResponse>> pageStudents(@RequestParam(required = false) String grade,
                                                                                @RequestParam(required = false) String className,
                                                                                @RequestParam(required = false) String status,
                                                                                @RequestParam(required = false) String keyword,
                                                                                @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                                @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(platformService.pageStudents(grade, className, status, keyword, page, size));
    }

    @PostMapping("/files/upload")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR
    })
    public ApiResponse<PlatformFileUploadResponse> upload(@RequestParam(required = false) String bizType,
                                                          @RequestParam(required = false) Long bizId,
                                                          @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("文件上传成功", platformService.upload(bizType, bizId, file));
    }

    @GetMapping("/files/page")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR
    })
    public ApiResponse<PageResponse<PlatformFileUploadRecordResponse>> pageUploadRecords(@RequestParam(required = false) String bizType,
                                                                                          @Positive(message = "业务ID必须大于 0") @RequestParam(required = false) Long bizId,
                                                                                          @RequestParam(required = false) String uploaderKeyword,
                                                                                          @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                                          @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(platformService.pageUploadRecords(bizType, bizId, uploaderKeyword, page, size));
    }

    @PostMapping("/files/{id}/archive")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PlatformFileUploadRecordResponse> archiveUploadRecord(@Positive(message = "上传记录ID必须大于 0") @PathVariable Long id) {
        return ApiResponse.success("上传记录已归档", platformService.archiveUploadRecord(id));
    }

    @DeleteMapping("/files/{id}")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<Void> deleteUploadRecord(@Positive(message = "上传记录ID必须大于 0") @PathVariable Long id) {
        platformService.deleteUploadRecord(id);
        return ApiResponse.success("上传记录已删除", null);
    }

    @PostMapping("/import-tasks")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PlatformImportTaskReceiptResponse> createImportTask(@Valid @RequestBody PlatformImportTaskCreateRequest request) {
        return ApiResponse.success("导入任务已创建", platformService.createImportTask(request));
    }

    @PutMapping("/import-tasks/{taskId}")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PlatformImportTaskReceiptResponse> updateImportTask(@Positive(message = "导入任务ID必须大于 0") @PathVariable Long taskId,
                                                                           @Valid @RequestBody PlatformImportTaskUpdateRequest request) {
        return ApiResponse.success("导入任务状态已更新", platformService.updateImportTask(taskId, request));
    }

    @GetMapping("/import-tasks/page")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PageResponse<DataImportTaskResponse>> pageImportTasks(@RequestParam(required = false) String taskType,
                                                                             @RequestParam(required = false) String status,
                                                                             @RequestParam(required = false) String ownerKeyword,
                                                                             @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                             @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(platformService.pageImportTasks(taskType, status, ownerKeyword, page, size));
    }

    @GetMapping("/import-tasks/{taskId}/errors/page")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PageResponse<DataImportErrorItemResponse>> pageImportErrors(@Positive(message = "导入任务ID必须大于 0") @PathVariable Long taskId,
                                                                                    @Min(value = 1, message = "rowNumber 必须大于 0") @RequestParam(required = false) Integer rowNumber,
                                                                                    @RequestParam(required = false) String fieldName,
                                                                                    @RequestParam(required = false) String keyword,
                                                                                    @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                                    @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(platformService.pageImportErrors(taskId, rowNumber, fieldName, keyword, page, size));
    }

    @PostMapping("/import-tasks/{taskId}/errors")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PlatformImportTaskReceiptResponse> createImportError(@Positive(message = "导入任务ID必须大于 0") @PathVariable Long taskId,
                                                                            @Valid @RequestBody PlatformImportErrorCreateRequest request) {
        return ApiResponse.success("导入错误明细已登记", platformService.createImportError(taskId, request));
    }

    @GetMapping("/import-tasks/{taskId}/receipt")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR
    })
    public ApiResponse<PlatformImportTaskReceiptResponse> getImportTaskReceipt(@Positive(message = "导入任务ID必须大于 0") @PathVariable Long taskId) {
        return ApiResponse.success(platformService.getImportTaskReceipt(taskId));
    }

    @GetMapping("/audit/admin-operation-logs/page")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PageResponse<AdminOperationLogResponse>> pageOperationLogs(@RequestParam(required = false) String module,
                                                                                  @RequestParam(required = false) String action,
                                                                                  @RequestParam(required = false) String operatorRole,
                                                                                  @RequestParam(required = false) String targetKeyword,
                                                                                  @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                                  @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(platformService.pageAdminOperationLogs(module, action, operatorRole, targetKeyword, page, size));
    }

    @GetMapping("/audit/login-logs/page")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN
    })
    public ApiResponse<PageResponse<PlatformLoginAuditLogResponse>> pageLoginAuditLogs(@RequestParam(required = false) String action,
                                                                                        @RequestParam(required = false) String result,
                                                                                        @RequestParam(required = false) String keyword,
                                                                                        @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                                        @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(platformService.pageLoginAuditLogs(action, result, keyword, page, size));
    }

    @GetMapping("/audit/approval-logs/{requestId}")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR
    })
    public ApiResponse<List<ApprovalHistoryResponse>> approvalHistory(@Positive(message = "申请ID必须大于 0") @PathVariable Long requestId) {
        return ApiResponse.success(platformService.approvalHistory(requestId));
    }

    @GetMapping("/notifications/send-records")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR
    })
    public ApiResponse<List<PlatformNotificationSendRecordResponse>> listNotificationSendRecords() {
        return ApiResponse.success(platformService.listNotificationSendRecords());
    }

    @PostMapping("/notifications/send")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR
    })
    public ApiResponse<PlatformNotificationSendRecordResponse> sendNotification(@Valid @RequestBody PlatformNotificationSendRequest request) {
        return ApiResponse.success("通知发送记录已创建", platformService.sendNotification(request));
    }

    @GetMapping("/notifications/send-records/page")
    @RequireRoles({
            RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR
    })
    public ApiResponse<PageResponse<PlatformNotificationSendRecordResponse>> pageNotificationSendRecords(@RequestParam(required = false) String channel,
                                                                                                          @RequestParam(required = false) String status,
                                                                                                          @RequestParam(required = false) String targetKeyword,
                                                                                                          @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                                                          @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(platformService.pageNotificationSendRecords(channel, status, targetKeyword, page, size));
    }
}
