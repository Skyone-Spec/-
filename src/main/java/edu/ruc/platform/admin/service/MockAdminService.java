package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.AdminKnowledgeItemResponse;
import edu.ruc.platform.admin.dto.AdminKnowledgeFilterRequest;
import edu.ruc.platform.admin.dto.AdminKnowledgeUpsertRequest;
import edu.ruc.platform.admin.dto.AdminKnowledgeStatsResponse;
import edu.ruc.platform.admin.dto.AdminNoticeFilterRequest;
import edu.ruc.platform.admin.dto.AdminNoticeCreateRequest;
import edu.ruc.platform.admin.dto.AdminNoticeStatsResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogFilterRequest;
import edu.ruc.platform.admin.dto.AdminOperationLogResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogStatsResponse;
import edu.ruc.platform.admin.dto.AdminStatsResponse;
import edu.ruc.platform.admin.dto.AdvisorScopeFilterRequest;
import edu.ruc.platform.admin.dto.AdvisorScopeBindingResponse;
import edu.ruc.platform.admin.dto.AdvisorScopeBindingUpsertRequest;
import edu.ruc.platform.admin.dto.AdvisorScopeStatsResponse;
import edu.ruc.platform.admin.dto.AdvisorScopeAdvisorStatsResponse;
import edu.ruc.platform.admin.dto.DataImportErrorFilterRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemCreateRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemResponse;
import edu.ruc.platform.admin.dto.DataImportTaskFilterRequest;
import edu.ruc.platform.admin.dto.DataImportTaskStatsResponse;
import edu.ruc.platform.admin.dto.DataImportTaskCreateRequest;
import edu.ruc.platform.admin.dto.DataImportTaskResponse;
import edu.ruc.platform.admin.dto.DataImportTaskUpdateRequest;
import edu.ruc.platform.admin.dto.KnowledgeAttachmentResponse;
import edu.ruc.platform.admin.dto.KnowledgeCategoryStatsResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogModuleStatsResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogRoleStatsResponse;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.DataImportTaskStatus;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.mock.MockDataStore;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockAdminService implements AdminApplicationService {

    private final MockDataStore mockDataStore;
    private final CurrentUserService currentUserService;
    private final AtomicLong noticeIdGenerator = new AtomicLong(10);
    private final AtomicLong knowledgeIdGenerator = new AtomicLong(200);
    private final AtomicLong importTaskIdGenerator = new AtomicLong(20);
    private final AtomicLong advisorScopeIdGenerator = new AtomicLong(10);
    private final AtomicLong operationLogIdGenerator = new AtomicLong(10);
    private final AtomicLong knowledgeAttachmentIdGenerator = new AtomicLong(50);
    private final AtomicLong importErrorIdGenerator = new AtomicLong(80);
    private final List<TargetedNoticeResponse> notices = new ArrayList<>();
    private final List<AdminKnowledgeItemResponse> knowledgeItems = new ArrayList<>();
    private final List<DataImportTaskResponse> importTasks = new ArrayList<>();
    private final List<AdminOperationLogResponse> operationLogs = new ArrayList<>();
    private final List<KnowledgeAttachmentResponse> knowledgeAttachments = new ArrayList<>(List.of(
            new KnowledgeAttachmentResponse(51L, 101L, "party-process.pdf", "application/pdf", 1024L, "/uploads/knowledge/101/party-process.pdf", "胡浩老师", LocalDateTime.of(2026, 3, 22, 11, 0))
    ));
    private final List<DataImportErrorItemResponse> importErrors = new ArrayList<>(List.of(
            new DataImportErrorItemResponse(81L, 2L, 7, "title", "标题为空", null, LocalDateTime.of(2026, 3, 23, 9, 5)),
            new DataImportErrorItemResponse(82L, 2L, 12, "officialUrl", "URL 格式不合法", "htp://bad-url", LocalDateTime.of(2026, 3, 23, 9, 6))
    ));
    private final Map<Long, ImportExecutionContext> importExecutionContexts = new HashMap<>();
    private final List<AdvisorScopeBindingResponse> advisorScopes = new ArrayList<>(List.of(
            new AdvisorScopeBindingResponse(1L, "advisor01", "王老师", "2023级", "计科一班", 10001L),
            new AdvisorScopeBindingResponse(2L, "advisor02", "赵老师", "2023级", "计科二班", 10002L)
    ));

    @Override
    public List<TargetedNoticeResponse> listNotices() {
        if (notices.isEmpty()) {
            notices.addAll(mockDataStore.notices());
        }
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return notices.stream()
                .filter(item -> canViewNotice(user, item))
                .toList();
    }

    @Override
    public PageResponse<TargetedNoticeResponse> pageNotices(AdminNoticeFilterRequest request, int page, int size) {
        List<TargetedNoticeResponse> filtered = filterNotices(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdminNoticeStatsResponse noticeStats(AdminNoticeFilterRequest request) {
        List<TargetedNoticeResponse> filtered = filterNotices(request);
        return new AdminNoticeStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> item.tags() != null && !item.tags().isEmpty()).count(),
                (int) filtered.stream().filter(item -> item.targetDescription() != null && item.targetDescription().contains("级")).count(),
                (int) filtered.stream().filter(item -> "毕业生".equals(item.targetDescription())).count()
        );
    }

    @Override
    public TargetedNoticeResponse createNotice(AdminNoticeCreateRequest request) {
        TargetedNoticeResponse response = new TargetedNoticeResponse(
                noticeIdGenerator.incrementAndGet(),
                request.title(),
                request.summary(),
                request.tags(),
                request.targetDescription(),
                resolvePriority(request.tags(), request.targetDescription()),
                resolveMatchedRules(request.targetDescription(), request.tags()),
                resolveDeliveryChannels(request.tags()),
                LocalDateTime.now()
        );
        notices.add(0, response);
        writeOperationLog("NOTICE", "CREATE", response.title(), "SUCCESS", response.targetDescription());
        return response;
    }

    @Override
    public List<AdminKnowledgeItemResponse> listKnowledgeItems(AuthenticatedUser user) {
        initializeKnowledgeItems();
        return knowledgeItems.stream()
                .filter(item -> canViewKnowledgeItem(user, item))
                .toList();
    }

    @Override
    public PageResponse<AdminKnowledgeItemResponse> pageKnowledgeItems(AuthenticatedUser user, AdminKnowledgeFilterRequest request, int page, int size) {
        List<AdminKnowledgeItemResponse> filtered = filterKnowledgeItems(user, request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdminKnowledgeStatsResponse knowledgeStats(AuthenticatedUser user, AdminKnowledgeFilterRequest request) {
        List<AdminKnowledgeItemResponse> filtered = filterKnowledgeItems(user, request);
        List<KnowledgeCategoryStatsResponse> categoryStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(AdminKnowledgeItemResponse::category))
                .entrySet()
                .stream()
                .map(entry -> new KnowledgeCategoryStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        (int) entry.getValue().stream().filter(AdminKnowledgeItemResponse::published).count()
                ))
                .sorted(java.util.Comparator.comparing(KnowledgeCategoryStatsResponse::itemCount).reversed())
                .toList();
        long totalAttachments = filtered.stream()
                .map(AdminKnowledgeItemResponse::id)
                .mapToLong(id -> knowledgeAttachments.stream().filter(item -> item.knowledgeId().equals(id)).count())
                .sum();
        return new AdminKnowledgeStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(AdminKnowledgeItemResponse::published).count(),
                (int) filtered.stream().filter(item -> !item.published()).count(),
                (int) totalAttachments,
                categoryStats
        );
    }

    @Override
    public AdminKnowledgeItemResponse createKnowledgeItem(AdminKnowledgeUpsertRequest request) {
        initializeKnowledgeItems();
        validateKnowledgeRequest(request);
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        AdminKnowledgeItemResponse response = new AdminKnowledgeItemResponse(
                knowledgeIdGenerator.incrementAndGet(),
                request.title(),
                request.category(),
                Boolean.TRUE.equals(request.published()),
                request.officialUrl(),
                request.sourceFileName(),
                request.audienceScope(),
                user.name()
        );
        knowledgeItems.add(0, response);
        writeOperationLog("KNOWLEDGE", "CREATE", response.title(), "SUCCESS", response.sourceFileName());
        return response;
    }

    @Override
    public AdminKnowledgeItemResponse updateKnowledgeItem(Long id, AdminKnowledgeUpsertRequest request) {
        initializeKnowledgeItems();
        validateKnowledgeRequest(request);
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        for (int i = 0; i < knowledgeItems.size(); i++) {
            AdminKnowledgeItemResponse item = knowledgeItems.get(i);
            if (item.id().equals(id)) {
                AdminKnowledgeItemResponse updated = new AdminKnowledgeItemResponse(
                        id,
                        request.title(),
                        request.category(),
                        Boolean.TRUE.equals(request.published()),
                        request.officialUrl(),
                        request.sourceFileName(),
                        request.audienceScope(),
                        user.name()
                );
                knowledgeItems.set(i, updated);
                writeOperationLog("KNOWLEDGE", "UPDATE", updated.title(), "SUCCESS", updated.sourceFileName());
                return updated;
            }
        }
        throw new BusinessException("知识条目不存在: " + id);
    }

    @Override
    public List<KnowledgeAttachmentResponse> listKnowledgeAttachments(Long knowledgeId) {
        initializeKnowledgeItems();
        boolean exists = knowledgeItems.stream().anyMatch(item -> item.id().equals(knowledgeId));
        if (!exists) {
            throw new BusinessException("知识条目不存在: " + knowledgeId);
        }
        return knowledgeAttachments.stream()
                .filter(item -> item.knowledgeId().equals(knowledgeId))
                .toList();
    }

    @Override
    public KnowledgeAttachmentResponse uploadKnowledgeAttachment(Long knowledgeId, MultipartFile file) {
        initializeKnowledgeItems();
        boolean exists = knowledgeItems.stream().anyMatch(item -> item.id().equals(knowledgeId));
        if (!exists) {
            throw new BusinessException("知识条目不存在: " + knowledgeId);
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > 30L * 1024 * 1024) {
            throw new BusinessException("知识附件大小不能超过 30MB");
        }
        validateKnowledgeAttachment(file);
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        KnowledgeAttachmentResponse response = new KnowledgeAttachmentResponse(
                knowledgeAttachmentIdGenerator.incrementAndGet(),
                knowledgeId,
                file.getOriginalFilename() == null ? "unknown-file" : file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                "/uploads/knowledge/" + knowledgeId + "/" + System.currentTimeMillis() + "-" + (file.getOriginalFilename() == null ? "unknown-file" : file.getOriginalFilename()),
                user.name(),
                LocalDateTime.now()
        );
        knowledgeAttachments.add(0, response);
        writeOperationLog("KNOWLEDGE_ATTACHMENT", "UPLOAD", "knowledge#" + knowledgeId, "SUCCESS", response.fileName());
        return response;
    }

    @Override
    public void deleteKnowledgeAttachment(Long attachmentId) {
        KnowledgeAttachmentResponse target = knowledgeAttachments.stream()
                .filter(item -> item.id().equals(attachmentId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("知识附件不存在: " + attachmentId));
        knowledgeAttachments.removeIf(item -> item.id().equals(attachmentId));
        writeOperationLog("KNOWLEDGE_ATTACHMENT", "DELETE", "attachment#" + attachmentId, "SUCCESS", target.fileName());
    }

    @Override
    public List<AdvisorScopeBindingResponse> listAdvisorScopes(String advisorUsername, String grade, String className) {
        String normalizedAdvisorUsername = QueryFilterSupport.trimToNull(advisorUsername);
        String normalizedGrade = QueryFilterSupport.trimToNull(grade);
        String normalizedClassName = QueryFilterSupport.trimToNull(className);
        return advisorScopes.stream()
                .filter(item -> normalizedAdvisorUsername == null || normalizedAdvisorUsername.equalsIgnoreCase(item.advisorUsername()))
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.grade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.className()))
                .toList();
    }

    @Override
    public PageResponse<AdvisorScopeBindingResponse> pageAdvisorScopes(AdvisorScopeFilterRequest request, int page, int size) {
        List<AdvisorScopeBindingResponse> filtered = listAdvisorScopes(request.advisorUsername(), request.grade(), request.className());
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdvisorScopeStatsResponse advisorScopeStats(AdvisorScopeFilterRequest request) {
        List<AdvisorScopeBindingResponse> filtered = listAdvisorScopes(request.advisorUsername(), request.grade(), request.className());
        List<AdvisorScopeAdvisorStatsResponse> advisorStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        item -> item.advisorUsername() + "|" + (item.advisorName() == null ? "" : item.advisorName())
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    AdvisorScopeBindingResponse first = entry.getValue().get(0);
                    long studentCount = entry.getValue().stream().map(AdvisorScopeBindingResponse::studentId).distinct().count();
                    return new AdvisorScopeAdvisorStatsResponse(first.advisorUsername(), first.advisorName(), entry.getValue().size(), (int) studentCount);
                })
                .sorted(java.util.Comparator.comparing(AdvisorScopeAdvisorStatsResponse::bindingCount).reversed())
                .toList();
        return new AdvisorScopeStatsResponse(
                filtered.size(),
                (int) filtered.stream().map(AdvisorScopeBindingResponse::advisorUsername).distinct().count(),
                (int) filtered.stream().map(AdvisorScopeBindingResponse::grade).filter(java.util.Objects::nonNull).distinct().count(),
                (int) filtered.stream().map(AdvisorScopeBindingResponse::className).filter(java.util.Objects::nonNull).distinct().count(),
                advisorStats
        );
    }

    @Override
    public AdvisorScopeBindingResponse createAdvisorScope(AdvisorScopeBindingUpsertRequest request) {
        AdvisorScopeBindingResponse response = new AdvisorScopeBindingResponse(
                advisorScopeIdGenerator.incrementAndGet(),
                request.advisorUsername(),
                request.advisorName(),
                request.grade(),
                request.className(),
                request.studentId()
        );
        advisorScopes.add(0, response);
        writeOperationLog("ADVISOR_SCOPE", "CREATE", buildAdvisorScopeTarget(response), "SUCCESS", response.advisorUsername());
        return response;
    }

    @Override
    public AdvisorScopeBindingResponse updateAdvisorScope(Long id, AdvisorScopeBindingUpsertRequest request) {
        for (int i = 0; i < advisorScopes.size(); i++) {
            AdvisorScopeBindingResponse item = advisorScopes.get(i);
            if (item.id().equals(id)) {
                AdvisorScopeBindingResponse updated = new AdvisorScopeBindingResponse(
                        id,
                        request.advisorUsername(),
                        request.advisorName(),
                        request.grade(),
                        request.className(),
                        request.studentId()
                );
                advisorScopes.set(i, updated);
                writeOperationLog("ADVISOR_SCOPE", "UPDATE", buildAdvisorScopeTarget(updated), "SUCCESS", updated.advisorUsername());
                return updated;
            }
        }
        throw new BusinessException("班主任负责范围不存在: " + id);
    }

    @Override
    public void deleteAdvisorScope(Long id) {
        boolean removed = advisorScopes.removeIf(item -> item.id().equals(id));
        if (!removed) {
            throw new BusinessException("班主任负责范围不存在: " + id);
        }
        writeOperationLog("ADVISOR_SCOPE", "DELETE", "advisor-scope#" + id, "SUCCESS", null);
    }

    @Override
    public AdminStatsResponse stats(int pendingApprovals) {
        return new AdminStatsResponse(
                1000,
                pendingApprovals,
                listNotices().size(),
                mockDataStore.knowledgeDocuments().size()
        );
    }

    @Override
    public List<AdminOperationLogResponse> listOperationLogs() {
        initializeOperationLogs();
        return List.copyOf(operationLogs);
    }

    @Override
    public PageResponse<AdminOperationLogResponse> pageOperationLogs(AdminOperationLogFilterRequest request, int page, int size) {
        List<AdminOperationLogResponse> filtered = filterOperationLogs(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdminOperationLogStatsResponse operationLogStats(AdminOperationLogFilterRequest request) {
        List<AdminOperationLogResponse> filtered = filterOperationLogs(request);
        List<AdminOperationLogModuleStatsResponse> moduleStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(AdminOperationLogResponse::module))
                .entrySet()
                .stream()
                .map(entry -> new AdminOperationLogModuleStatsResponse(entry.getKey(), entry.getValue().size()))
                .sorted(java.util.Comparator.comparing(AdminOperationLogModuleStatsResponse::count).reversed()
                        .thenComparing(AdminOperationLogModuleStatsResponse::module))
                .toList();
        List<AdminOperationLogRoleStatsResponse> roleStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(AdminOperationLogResponse::operatorRole))
                .entrySet()
                .stream()
                .map(entry -> new AdminOperationLogRoleStatsResponse(entry.getKey(), entry.getValue().size()))
                .sorted(java.util.Comparator.comparing(AdminOperationLogRoleStatsResponse::count).reversed()
                        .thenComparing(AdminOperationLogRoleStatsResponse::operatorRole))
                .toList();
        return new AdminOperationLogStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> "SUCCESS".equalsIgnoreCase(item.result())).count(),
                (int) filtered.stream().filter(item -> "FAILED".equalsIgnoreCase(item.result())).count(),
                moduleStats.size(),
                roleStats.size(),
                moduleStats,
                roleStats
        );
    }

    @Override
    public List<DataImportTaskResponse> listImportTasks() {
        initializeImportTasks();
        return List.copyOf(importTasks);
    }

    @Override
    public PageResponse<DataImportTaskResponse> pageImportTasks(DataImportTaskFilterRequest request, int page, int size) {
        List<DataImportTaskResponse> filtered = filterImportTasks(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public DataImportTaskStatsResponse importTaskStats(DataImportTaskFilterRequest request) {
        List<DataImportTaskResponse> filtered = filterImportTasks(request);
        return new DataImportTaskStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.CREATED.name().equals(item.status())).count(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.RUNNING.name().equals(item.status())).count(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.SUCCESS.name().equals(item.status())).count(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.PARTIAL_SUCCESS.name().equals(item.status())).count(),
                (int) filtered.stream().filter(item -> DataImportTaskStatus.FAILED.name().equals(item.status())).count(),
                filtered.stream().mapToInt(DataImportTaskResponse::totalRows).sum(),
                filtered.stream().mapToInt(DataImportTaskResponse::successRows).sum(),
                filtered.stream().mapToInt(DataImportTaskResponse::failedRows).sum()
        );
    }

    @Override
    public DataImportTaskResponse createImportTask(DataImportTaskCreateRequest request) {
        initializeImportTasks();
        validateImportTaskCreateRequest(request);
        DataImportTaskResponse response = new DataImportTaskResponse(
                importTaskIdGenerator.incrementAndGet(),
                request.taskType(),
                request.fileName(),
                resolveFileType(request.fileName()),
                resolveTemplateName(request.taskType()),
                resolveTemplateDownloadUrl(request.taskType()),
                DataImportTaskStatus.CREATED.name(),
                request.totalRows(),
                0,
                0,
                0,
                request.owner(),
                LocalDateTime.now()
        );
        importTasks.add(0, response);
        writeOperationLog("IMPORT_TASK", "CREATE", response.fileName(), response.status(), response.taskType());
        return response;
    }

    @Override
    public DataImportTaskResponse updateImportTask(Long id, DataImportTaskUpdateRequest request) {
        initializeImportTasks();
        for (int i = 0; i < importTasks.size(); i++) {
            DataImportTaskResponse item = importTasks.get(i);
            if (item.id().equals(id)) {
                validateImportTaskTransition(item, request);
                validateImportTaskRows(item.totalRows(), request.successRows(), request.failedRows());
                DataImportTaskResponse updated = new DataImportTaskResponse(
                        item.id(),
                        item.taskType(),
                        item.fileName(),
                        item.fileType(),
                        item.templateName(),
                        item.templateDownloadUrl(),
                        DataImportTaskStatus.from(request.status()).name(),
                        item.totalRows(),
                        request.successRows(),
                        request.failedRows(),
                        calculateProgressPercent(item.totalRows(), request.successRows(), request.failedRows()),
                        item.owner(),
                        item.createdAt()
                );
                importTasks.set(i, updated);
                writeOperationLog("IMPORT_TASK", "UPDATE", updated.fileName(), updated.status(), request.errorSummary());
                return updated;
            }
        }
        throw new BusinessException("导入任务不存在: " + id);
    }

    @Override
    public List<DataImportErrorItemResponse> listImportErrors(Long taskId) {
        initializeImportTasks();
        boolean exists = importTasks.stream().anyMatch(item -> item.id().equals(taskId));
        if (!exists) {
            throw new BusinessException("导入任务不存在: " + taskId);
        }
        return importErrors.stream()
                .filter(item -> item.taskId().equals(taskId))
                .toList();
    }

    @Override
    public PageResponse<DataImportErrorItemResponse> pageImportErrors(Long taskId, DataImportErrorFilterRequest request, int page, int size) {
        List<DataImportErrorItemResponse> filtered = filterImportErrors(taskId, request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public DataImportErrorItemResponse createImportError(Long taskId, DataImportErrorItemCreateRequest request) {
        initializeImportTasks();
        DataImportTaskResponse task = importTasks.stream()
                .filter(item -> item.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("导入任务不存在: " + taskId));
        if (request.rowNumber() > task.totalRows()) {
            throw new BusinessException("错误行号不能超过导入任务总行数");
        }
        DataImportErrorItemResponse response = new DataImportErrorItemResponse(
                importErrorIdGenerator.incrementAndGet(),
                taskId,
                request.rowNumber(),
                request.fieldName(),
                request.errorMessage(),
                request.rawValue(),
                LocalDateTime.now()
        );
        importErrors.add(response);
        writeOperationLog("IMPORT_TASK", "ADD_ERROR", task.fileName(), "SUCCESS", "row=" + request.rowNumber());
        return response;
    }

    @Override
    public void replaceImportErrors(Long taskId, List<DataImportErrorItemCreateRequest> requests) {
        initializeImportTasks();
        DataImportTaskResponse task = importTasks.stream()
                .filter(item -> item.id().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("导入任务不存在: " + taskId));
        importErrors.removeIf(item -> item.taskId().equals(taskId));
        List<DataImportErrorItemCreateRequest> safeRequests = requests == null ? List.of() : requests;
        for (DataImportErrorItemCreateRequest request : safeRequests) {
            if (request.rowNumber() > task.totalRows()) {
                throw new BusinessException("错误行号不能超过导入任务总行数");
            }
            importErrors.add(new DataImportErrorItemResponse(
                    importErrorIdGenerator.incrementAndGet(),
                    taskId,
                    request.rowNumber(),
                    request.fieldName(),
                    request.errorMessage(),
                    request.rawValue(),
                    LocalDateTime.now()
            ));
        }
        writeOperationLog("IMPORT_TASK", "REPLACE_ERRORS", task.fileName(), "SUCCESS", "count=" + safeRequests.size());
    }

    @Override
    public void recordImportExecutionContext(Long taskId, String executionBatchNo, String callbackSource) {
        initializeImportTasks();
        boolean exists = importTasks.stream().anyMatch(item -> item.id().equals(taskId));
        if (!exists) {
            throw new BusinessException("导入任务不存在: " + taskId);
        }
        importExecutionContexts.put(taskId, new ImportExecutionContext(executionBatchNo, callbackSource, LocalDateTime.now()));
    }

    @Override
    public ImportExecutionContext getImportExecutionContext(Long taskId) {
        initializeImportTasks();
        return importExecutionContexts.get(taskId);
    }

    private void initializeKnowledgeItems() {
        if (!knowledgeItems.isEmpty()) {
            return;
        }
        knowledgeItems.addAll(mockDataStore.knowledgeDocuments().stream()
                .map(item -> new AdminKnowledgeItemResponse(
                        item.id(),
                        item.title(),
                        item.category(),
                        true,
                        item.officialUrl(),
                        item.title() + ".pdf",
                        "全体学生",
                        "胡浩老师"
                ))
                .toList());
        knowledgeItems.add(0, new AdminKnowledgeItemResponse(
                299L,
                "辅导员内部口径说明",
                "内部资料",
                false,
                "https://example.edu/internal",
                "internal-note.pdf",
                "辅导员",
                "系统管理员"
        ));
    }

    private void initializeImportTasks() {
        if (!importTasks.isEmpty()) {
            return;
        }
        importTasks.addAll(List.of(
                new DataImportTaskResponse(1L, "STUDENT_PROFILE", "students-info.xlsx", "xlsx", "学生档案导入模板", "/templates/import/student-profile.xlsx", DataImportTaskStatus.SUCCESS.name(), 1086, 1086, 0, 100, "系统管理员", LocalDateTime.of(2026, 3, 20, 16, 0)),
                new DataImportTaskResponse(2L, "KNOWLEDGE_BASE", "party-knowledge.xlsx", "xlsx", "知识库导入模板", "/templates/import/knowledge-base.xlsx", DataImportTaskStatus.PARTIAL_SUCCESS.name(), 28, 26, 2, 100, "胡浩老师", LocalDateTime.of(2026, 3, 23, 9, 0))
        ));
    }

    private String resolveFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String resolveTemplateName(String taskType) {
        return switch (taskType) {
            case "STUDENT_PROFILE" -> "学生档案导入模板";
            case "KNOWLEDGE_BASE" -> "知识库导入模板";
            case "NOTICE" -> "通知导入模板";
            case "ADVISOR_SCOPE" -> "班主任范围导入模板";
            default -> "通用导入模板";
        };
    }

    private String resolveTemplateDownloadUrl(String taskType) {
        return switch (taskType) {
            case "STUDENT_PROFILE" -> "/templates/import/student-profile.xlsx";
            case "KNOWLEDGE_BASE" -> "/templates/import/knowledge-base.xlsx";
            case "NOTICE" -> "/templates/import/notice.xlsx";
            case "ADVISOR_SCOPE" -> "/templates/import/advisor-scope.xlsx";
            default -> "/templates/import/general.xlsx";
        };
    }

    private int calculateProgressPercent(int totalRows, int successRows, int failedRows) {
        if (totalRows <= 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round((successRows + failedRows) * 100.0 / totalRows));
    }

    private boolean canViewKnowledgeItem(AuthenticatedUser user, AdminKnowledgeItemResponse item) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role()) || "CLASS_ADVISOR".equals(user.role())) {
            return true;
        }
        if (!"LEAGUE_SECRETARY".equals(user.role())) {
            return false;
        }
        if (!item.published()) {
            return false;
        }
        if (item.audienceScope() == null || item.audienceScope().isBlank()) {
            return true;
        }
        return item.audienceScope().contains("学生") || item.audienceScope().contains("全体");
    }

    private List<TargetedNoticeResponse> filterNotices(AdminNoticeFilterRequest request) {
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        String normalizedTag = QueryFilterSupport.trimToNull(request.tag());
        String normalizedTargetKeyword = QueryFilterSupport.trimToNull(request.targetKeyword());
        return listNotices().stream()
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.title(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.summary(), normalizedKeyword))
                .filter(item -> normalizedTag == null
                        || (item.tags() != null && item.tags().stream().anyMatch(tag -> tag.equalsIgnoreCase(normalizedTag))))
                .filter(item -> normalizedTargetKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.targetDescription(), normalizedTargetKeyword))
                .toList();
    }

    private boolean canViewNotice(AuthenticatedUser user, TargetedNoticeResponse item) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return true;
        }
        if (!"CLASS_ADVISOR".equals(user.role())) {
            return false;
        }
        if (item.targetDescription() == null || item.targetDescription().isBlank() || "全体学生".equals(item.targetDescription())) {
            return true;
        }
        return user.grade() != null && item.targetDescription().contains(user.grade());
    }

    private String resolvePriority(List<String> tags, String targetDescription) {
        if ((targetDescription != null && targetDescription.contains("级"))
                || tags.stream().anyMatch(tag -> "就业".equals(tag) || "实习".equals(tag))) {
            return "HIGH";
        }
        if (tags.stream().anyMatch(tag -> "流程".equals(tag) || "党团事务".equals(tag))) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> resolveMatchedRules(String targetDescription, List<String> tags) {
        List<String> rules = new ArrayList<>();
        if ("全体学生".equals(targetDescription)) {
            rules.add("全体学生");
        }
        if (targetDescription != null && targetDescription.contains("级")) {
            rules.add("年级匹配");
        }
        if (targetDescription != null && targetDescription.contains("计算机")) {
            rules.add("专业匹配");
        }
        if (tags.stream().anyMatch(tag -> "就业".equals(tag) || "实习".equals(tag))) {
            rules.add("就业标签");
        }
        if (tags.stream().anyMatch(tag -> "流程".equals(tag) || "党团事务".equals(tag))) {
            rules.add("党团事务标签");
        }
        return rules;
    }

    private List<String> resolveDeliveryChannels(List<String> tags) {
        if (tags.stream().anyMatch(tag -> "就业".equals(tag) || "实习".equals(tag))) {
            return List.of("IN_APP", "EMAIL", "WECHAT");
        }
        if (tags.stream().anyMatch(tag -> "流程".equals(tag) || "党团事务".equals(tag))) {
            return List.of("IN_APP", "EMAIL");
        }
        return List.of("IN_APP");
    }

    private List<DataImportTaskResponse> filterImportTasks(DataImportTaskFilterRequest request) {
        String normalizedTaskType = QueryFilterSupport.normalizeUpper(request.taskType());
        String normalizedStatus = QueryFilterSupport.normalizeUpper(request.status());
        String normalizedOwnerKeyword = QueryFilterSupport.trimToNull(request.ownerKeyword());
        return listImportTasks().stream()
                .filter(item -> normalizedTaskType == null || normalizedTaskType.equals(item.taskType()))
                .filter(item -> normalizedStatus == null || normalizedStatus.equals(item.status()))
                .filter(item -> normalizedOwnerKeyword == null || QueryFilterSupport.containsIgnoreCase(item.owner(), normalizedOwnerKeyword))
                .toList();
    }

    private List<AdminOperationLogResponse> filterOperationLogs(AdminOperationLogFilterRequest request) {
        String normalizedModule = QueryFilterSupport.trimToNull(request.module());
        String normalizedAction = QueryFilterSupport.trimToNull(request.action());
        String normalizedOperatorRole = QueryFilterSupport.trimToNull(request.operatorRole());
        String normalizedTargetKeyword = QueryFilterSupport.trimToNull(request.targetKeyword());
        return listOperationLogs().stream()
                .filter(item -> normalizedModule == null || normalizedModule.equalsIgnoreCase(item.module()))
                .filter(item -> normalizedAction == null || normalizedAction.equalsIgnoreCase(item.action()))
                .filter(item -> normalizedOperatorRole == null || normalizedOperatorRole.equalsIgnoreCase(item.operatorRole()))
                .filter(item -> normalizedTargetKeyword == null || QueryFilterSupport.containsIgnoreCase(item.target(), normalizedTargetKeyword))
                .toList();
    }

    private void validateImportTaskRows(int totalRows, int successRows, int failedRows) {
        if (successRows + failedRows > totalRows) {
            throw new BusinessException("成功行数与失败行数之和不能超过总行数");
        }
    }

    private void validateImportTaskCreateRequest(DataImportTaskCreateRequest request) {
        List<String> allowedTaskTypes = List.of("STUDENT_PROFILE", "KNOWLEDGE_BASE", "NOTICE", "ADVISOR_SCOPE");
        if (!allowedTaskTypes.contains(request.taskType())) {
            throw new BusinessException("导入任务类型仅支持 STUDENT_PROFILE、KNOWLEDGE_BASE、NOTICE、ADVISOR_SCOPE");
        }
        String lowerCaseFileName = request.fileName().toLowerCase(Locale.ROOT);
        if (!(lowerCaseFileName.endsWith(".xlsx") || lowerCaseFileName.endsWith(".xls") || lowerCaseFileName.endsWith(".csv"))) {
            throw new BusinessException("导入文件仅支持 xlsx、xls、csv");
        }
    }

    private void validateImportTaskTransition(DataImportTaskResponse current, DataImportTaskUpdateRequest request) {
        String currentStatus = current.status();
        String nextStatus = DataImportTaskStatus.from(request.status()).name();
        if (DataImportTaskStatus.SUCCESS.name().equals(currentStatus) || DataImportTaskStatus.FAILED.name().equals(currentStatus)) {
            throw new BusinessException("已完成的导入任务不允许再次更新状态");
        }
        if (DataImportTaskStatus.CREATED.name().equals(currentStatus) && DataImportTaskStatus.CREATED.name().equals(nextStatus)
                && (request.successRows() > 0 || request.failedRows() > 0)) {
            throw new BusinessException("CREATED 状态下成功行数和失败行数必须为 0");
        }
        if (DataImportTaskStatus.RUNNING.name().equals(nextStatus) && request.successRows() + request.failedRows() > current.totalRows()) {
            throw new BusinessException("运行中的导入任务处理行数不能超过总行数");
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

    private void validateKnowledgeRequest(AdminKnowledgeUpsertRequest request) {
        boolean published = Boolean.TRUE.equals(request.published());
        boolean hasOfficialUrl = request.officialUrl() != null && !request.officialUrl().isBlank();
        boolean hasSourceFile = request.sourceFileName() != null && !request.sourceFileName().isBlank();
        if (published && !hasOfficialUrl && !hasSourceFile) {
            throw new BusinessException("已发布知识条目必须提供官方链接或来源文件名");
        }
        if (hasOfficialUrl && !(request.officialUrl().startsWith("http://") || request.officialUrl().startsWith("https://"))) {
            throw new BusinessException("官方链接格式不正确");
        }
    }

    private void validateKnowledgeAttachment(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException("知识附件文件名不能为空");
        }
        String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);
        if (!(lowerCaseFileName.endsWith(".pdf")
                || lowerCaseFileName.endsWith(".doc")
                || lowerCaseFileName.endsWith(".docx")
                || lowerCaseFileName.endsWith(".xls")
                || lowerCaseFileName.endsWith(".xlsx")
                || lowerCaseFileName.endsWith(".txt"))) {
            throw new BusinessException("知识附件仅支持 pdf、doc、docx、xls、xlsx、txt");
        }
    }

    private List<DataImportErrorItemResponse> filterImportErrors(Long taskId, DataImportErrorFilterRequest request) {
        initializeImportTasks();
        boolean exists = importTasks.stream().anyMatch(item -> item.id().equals(taskId));
        if (!exists) {
            throw new BusinessException("导入任务不存在: " + taskId);
        }
        String normalizedFieldName = QueryFilterSupport.trimToNull(request.fieldName());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        return importErrors.stream()
                .filter(item -> item.taskId().equals(taskId))
                .filter(item -> request.rowNumber() == null || request.rowNumber().equals(item.rowNumber()))
                .filter(item -> normalizedFieldName == null || normalizedFieldName.equalsIgnoreCase(item.fieldName()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.errorMessage(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.rawValue(), normalizedKeyword))
                .toList();
    }

    private List<AdminKnowledgeItemResponse> filterKnowledgeItems(AuthenticatedUser user, AdminKnowledgeFilterRequest request) {
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        String normalizedCategory = QueryFilterSupport.trimToNull(request.category());
        return listKnowledgeItems(user).stream()
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.title(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.category(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.sourceFileName(), normalizedKeyword))
                .filter(item -> normalizedCategory == null || normalizedCategory.equalsIgnoreCase(item.category()))
                .filter(item -> request.published() == null || request.published().equals(item.published()))
                .toList();
    }

    private void initializeOperationLogs() {
        if (!operationLogs.isEmpty()) {
            return;
        }
        operationLogs.addAll(List.of(
                new AdminOperationLogResponse(1L, 1L, "系统管理员", "SUPER_ADMIN", "SYSTEM", "GRANT_ROLE", "团支书账号", "SUCCESS", "role=LEAGUE_SECRETARY", LocalDateTime.of(2026, 3, 21, 9, 30)),
                new AdminOperationLogResponse(2L, 20001L, "胡浩老师", "COUNSELOR", "KNOWLEDGE", "PUBLISH", "入党全流程说明", "SUCCESS", "published=true", LocalDateTime.of(2026, 3, 22, 14, 0)),
                new AdminOperationLogResponse(3L, 20001L, "胡浩老师", "COUNSELOR", "APPROVAL", "APPROVE", "证明申请#1001", "SUCCESS", "fromStatus=PENDING,toStatus=COUNSELOR_APPROVED", LocalDateTime.of(2026, 3, 23, 10, 10))
        ));
    }

    private void writeOperationLog(String module, String action, String target, String result, String detail) {
        initializeOperationLogs();
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        operationLogs.add(0, new AdminOperationLogResponse(
                operationLogIdGenerator.incrementAndGet(),
                user.userId(),
                user.name(),
                user.role(),
                module,
                action,
                target,
                result,
                detail,
                LocalDateTime.now()
        ));
    }

    private String buildAdvisorScopeTarget(AdvisorScopeBindingResponse response) {
        return response.advisorUsername() + "/" + response.grade() + "/" + response.className() + "/student#" + response.studentId();
    }
}
