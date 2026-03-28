package edu.ruc.platform.admin.service;

import edu.ruc.platform.admin.dto.AdminKnowledgeItemResponse;
import edu.ruc.platform.admin.dto.AdminKnowledgeFilterRequest;
import edu.ruc.platform.admin.dto.AdminKnowledgeUpsertRequest;
import edu.ruc.platform.admin.dto.AdminKnowledgeStatsResponse;
import edu.ruc.platform.admin.dto.AdminNoticeCreateRequest;
import edu.ruc.platform.admin.dto.AdminNoticeFilterRequest;
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
import edu.ruc.platform.admin.domain.DataImportTask;
import edu.ruc.platform.admin.domain.DataImportErrorItem;
import edu.ruc.platform.admin.domain.KnowledgeAttachment;
import edu.ruc.platform.admin.repository.DataImportErrorItemRepository;
import edu.ruc.platform.admin.repository.KnowledgeAttachmentRepository;
import edu.ruc.platform.admin.repository.AdminOperationLogRepository;
import edu.ruc.platform.admin.repository.DataImportTaskRepository;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.DataImportTaskStatus;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.knowledge.domain.KnowledgeDocument;
import edu.ruc.platform.certificate.repository.CertificateRequestRepository;
import edu.ruc.platform.knowledge.repository.KnowledgeDocumentRepository;
import edu.ruc.platform.notice.domain.Notice;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.notice.repository.NoticeRepository;
import edu.ruc.platform.student.domain.AdvisorScopeBinding;
import edu.ruc.platform.student.repository.AdvisorScopeBindingRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class AdminService implements AdminApplicationService {

    private final NoticeRepository noticeRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final KnowledgeAttachmentRepository knowledgeAttachmentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AdvisorScopeBindingRepository advisorScopeBindingRepository;
    private final CertificateRequestRepository certificateRequestRepository;
    private final AdminOperationLogRepository adminOperationLogRepository;
    private final DataImportTaskRepository dataImportTaskRepository;
    private final DataImportErrorItemRepository dataImportErrorItemRepository;
    private final CurrentUserService currentUserService;

    @Override
    public List<TargetedNoticeResponse> listNotices() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return noticeRepository.findAllByOrderByPublishTimeDesc()
                .stream()
                .filter(notice -> canViewNotice(user, notice))
                .map(notice -> new TargetedNoticeResponse(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getSummary(),
                        notice.getTag() == null ? List.of() : List.of(notice.getTag().split(",")),
                        buildTargetDescription(notice),
                        resolvePriority(notice),
                        resolveMatchedRules(notice),
                        resolveDeliveryChannels(notice),
                        notice.getPublishTime()
                ))
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
        List<Notice> filtered = filterNoticeEntities(request);
        return new AdminNoticeStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> item.getTag() != null && !item.getTag().isBlank()).count(),
                (int) filtered.stream().filter(item -> item.getTargetGrade() != null && !item.getTargetGrade().isBlank()).count(),
                (int) filtered.stream().filter(item -> Boolean.TRUE.equals(item.getTargetGraduateOnly())).count()
        );
    }

    @Override
    public TargetedNoticeResponse createNotice(AdminNoticeCreateRequest request) {
        Notice notice = new Notice();
        notice.setTitle(request.title());
        notice.setSummary(request.summary());
        notice.setTag(String.join(",", request.tags()));
        populateTargetFields(notice, request.targetDescription());
        notice.setPublishTime(LocalDateTime.now());
        notice = noticeRepository.save(notice);
        writeOperationLog("NOTICE", "CREATE", notice.getTitle(), "SUCCESS", request.targetDescription());
        return new TargetedNoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getSummary(),
                request.tags(),
                request.targetDescription(),
                resolvePriority(notice),
                resolveMatchedRules(notice),
                resolveDeliveryChannels(notice),
                notice.getPublishTime()
        );
    }

    @Override
    public List<AdminKnowledgeItemResponse> listKnowledgeItems(AuthenticatedUser user) {
        return knowledgeDocumentRepository.findAll()
                .stream()
                .filter(item -> canViewKnowledgeItem(user, item))
                .map(item -> new AdminKnowledgeItemResponse(
                        item.getId(),
                        item.getTitle(),
                        item.getCategory(),
                        Boolean.TRUE.equals(item.getPublished()),
                        item.getOfficialUrl(),
                        item.getSourceFileName(),
                        item.getAudienceScope(),
                        item.getUpdatedBy()
                ))
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
                .mapToLong(id -> knowledgeAttachmentRepository.findByKnowledgeIdOrderByCreatedAtDesc(id).size())
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
        KnowledgeDocument item = new KnowledgeDocument();
        populateKnowledgeItem(item, request);
        item = knowledgeDocumentRepository.save(item);
        writeOperationLog("KNOWLEDGE", "CREATE", item.getTitle(), "SUCCESS", request.sourceFileName());
        return toKnowledgeResponse(item);
    }

    @Override
    public AdminKnowledgeItemResponse updateKnowledgeItem(Long id, AdminKnowledgeUpsertRequest request) {
        KnowledgeDocument item = knowledgeDocumentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识条目不存在"));
        populateKnowledgeItem(item, request);
        item = knowledgeDocumentRepository.save(item);
        writeOperationLog("KNOWLEDGE", "UPDATE", item.getTitle(), "SUCCESS", request.sourceFileName());
        return toKnowledgeResponse(item);
    }

    @Override
    public List<KnowledgeAttachmentResponse> listKnowledgeAttachments(Long knowledgeId) {
        if (!knowledgeDocumentRepository.existsById(knowledgeId)) {
            throw new BusinessException("知识条目不存在");
        }
        return knowledgeAttachmentRepository.findByKnowledgeIdOrderByCreatedAtDesc(knowledgeId).stream()
                .map(this::toKnowledgeAttachmentResponse)
                .toList();
    }

    @Override
    public KnowledgeAttachmentResponse uploadKnowledgeAttachment(Long knowledgeId, MultipartFile file) {
        KnowledgeDocument knowledge = knowledgeDocumentRepository.findById(knowledgeId)
                .orElseThrow(() -> new BusinessException("知识条目不存在"));
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        long maxSize = 30L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("知识附件大小不能超过 30MB");
        }
        var user = currentUserService.requireCurrentUser();
        KnowledgeAttachment attachment = new KnowledgeAttachment();
        attachment.setKnowledgeId(knowledgeId);
        attachment.setFileName(file.getOriginalFilename() == null ? "unknown-file" : file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setStoragePath("/uploads/knowledge/" + knowledgeId + "/" + System.currentTimeMillis() + "-" + attachment.getFileName());
        attachment.setUploadedBy(user.name());
        attachment = knowledgeAttachmentRepository.save(attachment);
        writeOperationLog("KNOWLEDGE_ATTACHMENT", "UPLOAD", knowledge.getTitle(), "SUCCESS", attachment.getFileName());
        return toKnowledgeAttachmentResponse(attachment);
    }

    @Override
    public void deleteKnowledgeAttachment(Long attachmentId) {
        KnowledgeAttachment attachment = knowledgeAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("知识附件不存在"));
        knowledgeAttachmentRepository.delete(attachment);
        writeOperationLog("KNOWLEDGE_ATTACHMENT", "DELETE", "attachment#" + attachmentId, "SUCCESS", attachment.getFileName());
    }

    @Override
    public List<AdvisorScopeBindingResponse> listAdvisorScopes(String advisorUsername, String grade, String className) {
        String normalizedAdvisorUsername = QueryFilterSupport.trimToNull(advisorUsername);
        String normalizedGrade = QueryFilterSupport.trimToNull(grade);
        String normalizedClassName = QueryFilterSupport.trimToNull(className);
        return advisorScopeBindingRepository.findAll().stream()
                .filter(item -> normalizedAdvisorUsername == null || normalizedAdvisorUsername.equalsIgnoreCase(item.getAdvisorUsername()))
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.getGrade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.getClassName()))
                .map(this::toAdvisorScopeResponse)
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
        AdvisorScopeBinding binding = new AdvisorScopeBinding();
        binding.setAdvisorUsername(request.advisorUsername());
        binding.setAdvisorName(request.advisorName());
        binding.setGrade(request.grade());
        binding.setClassName(request.className());
        binding.setStudentId(request.studentId());
        binding = advisorScopeBindingRepository.save(binding);
        writeOperationLog("ADVISOR_SCOPE", "CREATE", buildAdvisorScopeTarget(binding), "SUCCESS", binding.getAdvisorUsername());
        return toAdvisorScopeResponse(binding);
    }

    @Override
    public AdvisorScopeBindingResponse updateAdvisorScope(Long id, AdvisorScopeBindingUpsertRequest request) {
        AdvisorScopeBinding binding = advisorScopeBindingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班主任负责范围不存在"));
        binding.setAdvisorUsername(request.advisorUsername());
        binding.setAdvisorName(request.advisorName());
        binding.setGrade(request.grade());
        binding.setClassName(request.className());
        binding.setStudentId(request.studentId());
        binding = advisorScopeBindingRepository.save(binding);
        writeOperationLog("ADVISOR_SCOPE", "UPDATE", buildAdvisorScopeTarget(binding), "SUCCESS", binding.getAdvisorUsername());
        return toAdvisorScopeResponse(binding);
    }

    @Override
    public void deleteAdvisorScope(Long id) {
        if (!advisorScopeBindingRepository.existsById(id)) {
            throw new BusinessException("班主任负责范围不存在");
        }
        AdvisorScopeBinding binding = advisorScopeBindingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班主任负责范围不存在"));
        advisorScopeBindingRepository.delete(binding);
        writeOperationLog("ADVISOR_SCOPE", "DELETE", buildAdvisorScopeTarget(binding), "SUCCESS", binding.getAdvisorUsername());
    }

    @Override
    public AdminStatsResponse stats(int pendingApprovals) {
        int pendingCount = pendingApprovals > 0
                ? pendingApprovals
                : Math.toIntExact(certificateRequestRepository.countByStatus("PENDING"));
        return new AdminStatsResponse(
                Math.toIntExact(studentProfileRepository.count()),
                pendingCount,
                Math.toIntExact(noticeRepository.count()),
                Math.toIntExact(knowledgeDocumentRepository.countByPublishedTrue())
        );
    }

    @Override
    public List<AdminOperationLogResponse> listOperationLogs() {
        return adminOperationLogRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(log -> new AdminOperationLogResponse(
                        log.getId(),
                        log.getOperatorId(),
                        log.getOperatorName(),
                        log.getOperatorRole(),
                        log.getModule(),
                        log.getAction(),
                        log.getTarget(),
                        log.getResult(),
                        log.getDetail(),
                        log.getCreatedAt()
                ))
                .toList();
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
        return dataImportTaskRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(task -> new DataImportTaskResponse(
                        task.getId(),
                        task.getTaskType(),
                        task.getFileName(),
                        resolveFileType(task.getFileName()),
                        resolveTemplateName(task.getTaskType()),
                        resolveTemplateDownloadUrl(task.getTaskType()),
                        task.getStatus(),
                        task.getTotalRows(),
                        task.getSuccessRows(),
                        task.getFailedRows(),
                        calculateProgressPercent(task.getTotalRows(), task.getSuccessRows(), task.getFailedRows()),
                        task.getOwnerName(),
                        task.getCreatedAt()
                ))
                .toList();
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
        validateImportTaskCreateRequest(request);
        DataImportTask task = new DataImportTask();
        task.setTaskType(request.taskType());
        task.setFileName(request.fileName());
        task.setStatus(DataImportTaskStatus.CREATED.name());
        task.setTotalRows(request.totalRows());
        task.setSuccessRows(0);
        task.setFailedRows(0);
        task.setOwnerId(0L);
        task.setOwnerName(request.owner());
        task = dataImportTaskRepository.save(task);
        writeOperationLog("IMPORT_TASK", "CREATE", task.getFileName(), task.getStatus(), task.getTaskType());
        return toImportTaskResponse(task);
    }

    @Override
    public DataImportTaskResponse updateImportTask(Long id, DataImportTaskUpdateRequest request) {
        DataImportTask task = dataImportTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        validateImportTaskRows(task.getTotalRows(), request.successRows(), request.failedRows());
        validateImportTaskTransition(toImportTaskResponse(task), request);
        task.setStatus(DataImportTaskStatus.from(request.status()).name());
        task.setSuccessRows(request.successRows());
        task.setFailedRows(request.failedRows());
        task.setErrorSummary(request.errorSummary());
        task = dataImportTaskRepository.save(task);
        writeOperationLog("IMPORT_TASK", "UPDATE", task.getFileName(), task.getStatus(), request.errorSummary());
        return toImportTaskResponse(task);
    }

    @Override
    public List<DataImportErrorItemResponse> listImportErrors(Long taskId) {
        if (!dataImportTaskRepository.existsById(taskId)) {
            throw new BusinessException("导入任务不存在");
        }
        return dataImportErrorItemRepository.findByTaskIdOrderByRowNumberAscCreatedAtAsc(taskId).stream()
                .map(this::toImportErrorResponse)
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
        DataImportTask task = dataImportTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        DataImportErrorItem item = new DataImportErrorItem();
        item.setTaskId(taskId);
        item.setRowNumber(request.rowNumber());
        item.setFieldName(request.fieldName());
        item.setErrorMessage(request.errorMessage());
        item.setRawValue(request.rawValue());
        item = dataImportErrorItemRepository.save(item);
        writeOperationLog("IMPORT_TASK", "ADD_ERROR", task.getFileName(), "SUCCESS", "row=" + request.rowNumber());
        return toImportErrorResponse(item);
    }

    private String buildTargetDescription(Notice notice) {
        if (notice.getTargetGrade() != null && notice.getTargetMajor() != null) {
            return notice.getTargetGrade() + "/" + notice.getTargetMajor();
        }
        if (notice.getTargetGrade() != null) {
            return notice.getTargetGrade();
        }
        if (notice.getTargetMajor() != null) {
            return notice.getTargetMajor();
        }
        return Boolean.TRUE.equals(notice.getTargetGraduateOnly()) ? "毕业生" : "全体学生";
    }

    private void populateTargetFields(Notice notice, String targetDescription) {
        if (targetDescription == null || targetDescription.isBlank() || "全体学生".equals(targetDescription)) {
            return;
        }
        if ("毕业生".equals(targetDescription)) {
            notice.setTargetGraduateOnly(Boolean.TRUE);
            return;
        }
        if (targetDescription.contains("/")) {
            String[] parts = targetDescription.split("/", 2);
            notice.setTargetGrade(parts[0].trim());
            notice.setTargetMajor(parts[1].trim());
            return;
        }
        if (targetDescription.endsWith("级")) {
            notice.setTargetGrade(targetDescription.trim());
            return;
        }
        notice.setTargetMajor(targetDescription.trim());
    }

    private void populateKnowledgeItem(KnowledgeDocument item, AdminKnowledgeUpsertRequest request) {
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        item.setTitle(request.title());
        item.setCategory(request.category());
        item.setContent(request.content());
        item.setOfficialUrl(request.officialUrl());
        item.setSourceFileName(request.sourceFileName());
        item.setAudienceScope(request.audienceScope());
        item.setUpdatedBy(currentUser.name());
        item.setPublished(request.published());
    }

    private AdminKnowledgeItemResponse toKnowledgeResponse(KnowledgeDocument item) {
        return new AdminKnowledgeItemResponse(
                item.getId(),
                item.getTitle(),
                item.getCategory(),
                Boolean.TRUE.equals(item.getPublished()),
                item.getOfficialUrl(),
                item.getSourceFileName(),
                item.getAudienceScope(),
                item.getUpdatedBy()
        );
    }

    private boolean canViewKnowledgeItem(AuthenticatedUser user, KnowledgeDocument item) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role()) || "CLASS_ADVISOR".equals(user.role())) {
            return true;
        }
        if (!"LEAGUE_SECRETARY".equals(user.role())) {
            return false;
        }
        if (!Boolean.TRUE.equals(item.getPublished())) {
            return false;
        }
        if (item.getAudienceScope() == null || item.getAudienceScope().isBlank()) {
            return true;
        }
        return item.getAudienceScope().contains("学生") || item.getAudienceScope().contains("全体");
    }

    private DataImportTaskResponse toImportTaskResponse(DataImportTask task) {
        return new DataImportTaskResponse(
                task.getId(),
                task.getTaskType(),
                task.getFileName(),
                resolveFileType(task.getFileName()),
                resolveTemplateName(task.getTaskType()),
                resolveTemplateDownloadUrl(task.getTaskType()),
                task.getStatus(),
                task.getTotalRows(),
                task.getSuccessRows(),
                task.getFailedRows(),
                calculateProgressPercent(task.getTotalRows(), task.getSuccessRows(), task.getFailedRows()),
                task.getOwnerName(),
                task.getCreatedAt()
        );
    }

    private String resolveFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(java.util.Locale.ROOT);
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

    private KnowledgeAttachmentResponse toKnowledgeAttachmentResponse(KnowledgeAttachment attachment) {
        return new KnowledgeAttachmentResponse(
                attachment.getId(),
                attachment.getKnowledgeId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getStoragePath(),
                attachment.getUploadedBy(),
                attachment.getCreatedAt()
        );
    }

    private DataImportErrorItemResponse toImportErrorResponse(DataImportErrorItem item) {
        return new DataImportErrorItemResponse(
                item.getId(),
                item.getTaskId(),
                item.getRowNumber(),
                item.getFieldName(),
                item.getErrorMessage(),
                item.getRawValue(),
                item.getCreatedAt()
        );
    }

    private List<DataImportErrorItemResponse> filterImportErrors(Long taskId, DataImportErrorFilterRequest request) {
        if (!dataImportTaskRepository.existsById(taskId)) {
            throw new BusinessException("导入任务不存在");
        }
        String normalizedFieldName = QueryFilterSupport.trimToNull(request.fieldName());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        return dataImportErrorItemRepository.findByTaskIdOrderByRowNumberAscCreatedAtAsc(taskId).stream()
                .map(this::toImportErrorResponse)
                .filter(item -> request.rowNumber() == null || request.rowNumber().equals(item.rowNumber()))
                .filter(item -> normalizedFieldName == null || normalizedFieldName.equalsIgnoreCase(item.fieldName()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.errorMessage(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.rawValue(), normalizedKeyword))
                .toList();
    }

    private AdvisorScopeBindingResponse toAdvisorScopeResponse(AdvisorScopeBinding binding) {
        return new AdvisorScopeBindingResponse(
                binding.getId(),
                binding.getAdvisorUsername(),
                binding.getAdvisorName(),
                binding.getGrade(),
                binding.getClassName(),
                binding.getStudentId()
        );
    }

    private List<TargetedNoticeResponse> filterNotices(AdminNoticeFilterRequest request) {
        return filterNoticeEntities(request).stream()
                .map(notice -> new TargetedNoticeResponse(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getSummary(),
                        notice.getTag() == null ? List.of() : List.of(notice.getTag().split(",")),
                        buildTargetDescription(notice),
                        resolvePriority(notice),
                        resolveMatchedRules(notice),
                        resolveDeliveryChannels(notice),
                        notice.getPublishTime()
                ))
                .toList();
    }

    private List<Notice> filterNoticeEntities(AdminNoticeFilterRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        String normalizedTag = QueryFilterSupport.trimToNull(request.tag());
        String normalizedTargetKeyword = QueryFilterSupport.trimToNull(request.targetKeyword());
        return noticeRepository.findAllByOrderByPublishTimeDesc().stream()
                .filter(item -> canViewNotice(user, item))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.getTitle(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getSummary(), normalizedKeyword))
                .filter(item -> normalizedTag == null
                        || (item.getTag() != null && Arrays.stream(item.getTag().split(","))
                        .map(String::trim)
                        .anyMatch(tag -> tag.equalsIgnoreCase(normalizedTag))))
                .filter(item -> normalizedTargetKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(buildTargetDescription(item), normalizedTargetKeyword))
                .toList();
    }

    private boolean canViewNotice(AuthenticatedUser user, Notice notice) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return true;
        }
        if (!"CLASS_ADVISOR".equals(user.role())) {
            return false;
        }
        String targetDescription = buildTargetDescription(notice);
        if (targetDescription == null || targetDescription.isBlank() || "全体学生".equals(targetDescription)) {
            return true;
        }
        return user.grade() != null && targetDescription.contains(user.grade());
    }

    private String resolvePriority(Notice notice) {
        if ((notice.getTargetGrade() != null && !notice.getTargetGrade().isBlank())
                || (notice.getTargetMajor() != null && !notice.getTargetMajor().isBlank())) {
            return "HIGH";
        }
        if (notice.getTag() != null && (notice.getTag().contains("流程") || notice.getTag().contains("党团事务"))) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> resolveMatchedRules(Notice notice) {
        List<String> rules = new java.util.ArrayList<>();
        if ((notice.getTargetGrade() == null || notice.getTargetGrade().isBlank())
                && (notice.getTargetMajor() == null || notice.getTargetMajor().isBlank())
                && !Boolean.TRUE.equals(notice.getTargetGraduateOnly())) {
            rules.add("全体学生");
        }
        if (notice.getTargetGrade() != null && !notice.getTargetGrade().isBlank()) {
            rules.add("年级匹配");
        }
        if (notice.getTargetMajor() != null && !notice.getTargetMajor().isBlank()) {
            rules.add("专业匹配");
        }
        if (notice.getTag() != null && (notice.getTag().contains("就业") || notice.getTag().contains("实习"))) {
            rules.add("就业标签");
        }
        if (notice.getTag() != null && (notice.getTag().contains("流程") || notice.getTag().contains("党团事务"))) {
            rules.add("党团事务标签");
        }
        if (Boolean.TRUE.equals(notice.getTargetGraduateOnly())) {
            rules.add("毕业生匹配");
        }
        return rules;
    }

    private List<String> resolveDeliveryChannels(Notice notice) {
        if (notice.getTag() != null && (notice.getTag().contains("就业") || notice.getTag().contains("实习"))) {
            return List.of("IN_APP", "EMAIL", "WECHAT");
        }
        if (notice.getTag() != null && (notice.getTag().contains("流程") || notice.getTag().contains("党团事务"))) {
            return List.of("IN_APP", "EMAIL");
        }
        return List.of("IN_APP");
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
        String lowerCaseFileName = request.fileName().toLowerCase(java.util.Locale.ROOT);
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

    private void writeOperationLog(String module, String action, String target, String result, String detail) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        edu.ruc.platform.admin.domain.AdminOperationLog log = new edu.ruc.platform.admin.domain.AdminOperationLog();
        log.setOperatorId(user.userId());
        log.setOperatorName(user.name());
        log.setOperatorRole(user.role());
        log.setModule(module);
        log.setAction(action);
        log.setTarget(target);
        log.setResult(result);
        log.setDetail(detail);
        adminOperationLogRepository.save(log);
    }

    private String buildAdvisorScopeTarget(AdvisorScopeBinding binding) {
        return binding.getAdvisorUsername() + "/" + binding.getGrade() + "/" + binding.getClassName() + "/student#" + binding.getStudentId();
    }
}
