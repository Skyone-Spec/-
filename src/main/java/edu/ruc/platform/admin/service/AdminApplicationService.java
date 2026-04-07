package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.AdminKnowledgeItemResponse;
import edu.ruc.platform.admin.dto.AdminKnowledgeFilterRequest;
import edu.ruc.platform.admin.dto.AdminNoticeCreateRequest;
import edu.ruc.platform.admin.dto.AdminNoticeFilterRequest;
import edu.ruc.platform.admin.dto.AdminNoticeStatsResponse;
import edu.ruc.platform.admin.dto.AdminKnowledgeUpsertRequest;
import edu.ruc.platform.admin.dto.AdminOperationLogFilterRequest;
import edu.ruc.platform.admin.dto.AdminOperationLogResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogStatsResponse;
import edu.ruc.platform.admin.dto.AdminKnowledgeStatsResponse;
import edu.ruc.platform.admin.dto.AdminStatsResponse;
import edu.ruc.platform.admin.dto.AdvisorScopeFilterRequest;
import edu.ruc.platform.admin.dto.AdvisorScopeBindingResponse;
import edu.ruc.platform.admin.dto.AdvisorScopeBindingUpsertRequest;
import edu.ruc.platform.admin.dto.AdvisorScopeStatsResponse;
import edu.ruc.platform.admin.dto.DataImportErrorFilterRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemCreateRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemResponse;
import edu.ruc.platform.admin.dto.DataImportTaskFilterRequest;
import edu.ruc.platform.admin.dto.DataImportTaskStatsResponse;
import edu.ruc.platform.admin.dto.DataImportTaskCreateRequest;
import edu.ruc.platform.admin.dto.DataImportTaskResponse;
import edu.ruc.platform.admin.dto.DataImportTaskUpdateRequest;
import edu.ruc.platform.admin.dto.KnowledgeAttachmentResponse;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminApplicationService {

    record ImportExecutionContext(String executionBatchNo, String callbackSource, java.time.LocalDateTime recordedAt) {
    }

    List<TargetedNoticeResponse> listNotices();

    PageResponse<TargetedNoticeResponse> pageNotices(AdminNoticeFilterRequest request, int page, int size);

    AdminNoticeStatsResponse noticeStats(AdminNoticeFilterRequest request);

    TargetedNoticeResponse createNotice(AdminNoticeCreateRequest request);

    List<AdminKnowledgeItemResponse> listKnowledgeItems(AuthenticatedUser user);

    PageResponse<AdminKnowledgeItemResponse> pageKnowledgeItems(AuthenticatedUser user, AdminKnowledgeFilterRequest request, int page, int size);

    AdminKnowledgeStatsResponse knowledgeStats(AuthenticatedUser user, AdminKnowledgeFilterRequest request);

    AdminKnowledgeItemResponse createKnowledgeItem(AdminKnowledgeUpsertRequest request);

    AdminKnowledgeItemResponse updateKnowledgeItem(Long id, AdminKnowledgeUpsertRequest request);

    List<KnowledgeAttachmentResponse> listKnowledgeAttachments(Long knowledgeId);

    KnowledgeAttachmentResponse uploadKnowledgeAttachment(Long knowledgeId, MultipartFile file);

    void deleteKnowledgeAttachment(Long attachmentId);

    List<AdvisorScopeBindingResponse> listAdvisorScopes(String advisorUsername, String grade, String className);

    PageResponse<AdvisorScopeBindingResponse> pageAdvisorScopes(AdvisorScopeFilterRequest request, int page, int size);

    AdvisorScopeStatsResponse advisorScopeStats(AdvisorScopeFilterRequest request);

    AdvisorScopeBindingResponse createAdvisorScope(AdvisorScopeBindingUpsertRequest request);

    AdvisorScopeBindingResponse updateAdvisorScope(Long id, AdvisorScopeBindingUpsertRequest request);

    void deleteAdvisorScope(Long id);

    AdminStatsResponse stats(int pendingApprovals);

    List<AdminOperationLogResponse> listOperationLogs();

    PageResponse<AdminOperationLogResponse> pageOperationLogs(AdminOperationLogFilterRequest request, int page, int size);

    AdminOperationLogStatsResponse operationLogStats(AdminOperationLogFilterRequest request);

    List<DataImportTaskResponse> listImportTasks();

    PageResponse<DataImportTaskResponse> pageImportTasks(DataImportTaskFilterRequest request, int page, int size);

    DataImportTaskStatsResponse importTaskStats(DataImportTaskFilterRequest request);

    DataImportTaskResponse createImportTask(DataImportTaskCreateRequest request);

    DataImportTaskResponse updateImportTask(Long id, DataImportTaskUpdateRequest request);

    List<DataImportErrorItemResponse> listImportErrors(Long taskId);

    PageResponse<DataImportErrorItemResponse> pageImportErrors(Long taskId, DataImportErrorFilterRequest request, int page, int size);

    DataImportErrorItemResponse createImportError(Long taskId, DataImportErrorItemCreateRequest request);

    void replaceImportErrors(Long taskId, List<DataImportErrorItemCreateRequest> requests);

    void recordImportExecutionContext(Long taskId, String executionBatchNo, String callbackSource);

    ImportExecutionContext getImportExecutionContext(Long taskId);
}
