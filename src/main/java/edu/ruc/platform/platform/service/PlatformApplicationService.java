package edu.ruc.platform.platform.service;

import edu.ruc.platform.admin.dto.AdminOperationLogResponse;
import edu.ruc.platform.admin.dto.DataImportErrorItemResponse;
import edu.ruc.platform.admin.dto.DataImportTaskResponse;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import edu.ruc.platform.common.api.PageResponse;
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
import edu.ruc.platform.platform.dto.PlatformUserResponse;
import edu.ruc.platform.platform.dto.PlatformUserStatsResponse;
import edu.ruc.platform.platform.dto.PlatformUserPasswordResetRequest;
import edu.ruc.platform.platform.dto.PlatformUserPasswordResetResponse;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyUpdateRequest;
import edu.ruc.platform.platform.dto.PlatformUserUpsertRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PlatformApplicationService {

    PlatformContractResponse contract();

    PlatformStudentUiContractResponse studentUiContract();

    PlatformPermissionSnapshotResponse currentPermissions();

    PlatformStudentDataScopeResponse currentStudentDataScope();

    PlatformStudentScopeCheckResponse checkStudentAccess(Long studentId);

    PlatformStudentScopeCheckResponse checkStudentScope(String grade, String className);

    List<PlatformRoleResponse> listRoles();

    PlatformSecurityPolicyResponse getSecurityPolicy();

    PlatformSecurityPolicyResponse updateSecurityPolicy(PlatformSecurityPolicyUpdateRequest request);

    PlatformUploadPolicyResponse getUploadPolicy();

    PlatformUploadPolicyResponse updateUploadPolicy(PlatformUploadPolicyUpdateRequest request);

    List<PlatformUserResponse> listUsers(String role, Boolean enabled, String keyword);

    PlatformUserDetailResponse getUser(Long userId);

    PlatformUserDetailResponse createUser(PlatformUserUpsertRequest request);

    PlatformUserDetailResponse updateUser(Long userId, PlatformUserUpsertRequest request);

    PlatformUserDetailResponse changeUserEnabled(Long userId, Boolean enabled);

    PlatformUserDetailResponse unlockUser(Long userId);

    PlatformUserPasswordResetResponse resetPassword(Long userId, PlatformUserPasswordResetRequest request);

    PageResponse<PlatformSessionResponse> pageSessions(Long userId, Boolean active, String keyword, int page, int size);

    PlatformSessionResponse revokeSession(Long sessionId);

    PageResponse<PlatformUserResponse> pageUsers(String role, Boolean enabled, String keyword, int page, int size);

    PlatformUserStatsResponse userStats(String role, Boolean enabled, String keyword);

    PlatformStudentQueryResponse getStudent(Long studentId);

    PageResponse<PlatformStudentQueryResponse> pageStudents(String grade, String className, String status, String keyword, int page, int size);

    PlatformFileUploadResponse upload(String bizType, Long bizId, MultipartFile file);

    PageResponse<PlatformFileUploadRecordResponse> pageUploadRecords(String bizType, Long bizId, String uploaderKeyword, int page, int size);

    PlatformFileUploadRecordResponse archiveUploadRecord(Long id);

    void deleteUploadRecord(Long id);

    PlatformImportTaskReceiptResponse createImportTask(PlatformImportTaskCreateRequest request);

    PlatformImportTaskReceiptResponse updateImportTask(Long taskId, PlatformImportTaskUpdateRequest request);

    PageResponse<DataImportTaskResponse> pageImportTasks(String taskType, String status, String ownerKeyword, int page, int size);

    PlatformImportTaskReceiptResponse getImportTaskReceipt(Long taskId);

    PlatformImportTaskReceiptResponse createImportError(Long taskId, PlatformImportErrorCreateRequest request);

    PageResponse<DataImportErrorItemResponse> pageImportErrors(Long taskId, Integer rowNumber, String fieldName, String keyword, int page, int size);

    PageResponse<AdminOperationLogResponse> pageAdminOperationLogs(String module, String action, String operatorRole, String targetKeyword, int page, int size);

    PageResponse<PlatformLoginAuditLogResponse> pageLoginAuditLogs(String action, String result, String keyword, int page, int size);

    List<ApprovalHistoryResponse> approvalHistory(Long requestId);

    PlatformNotificationSendRecordResponse sendNotification(PlatformNotificationSendRequest request);

    List<PlatformNotificationSendRecordResponse> listNotificationSendRecords();

    PageResponse<PlatformNotificationSendRecordResponse> pageNotificationSendRecords(String channel, String status, String targetKeyword, int page, int size);
}
