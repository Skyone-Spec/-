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
import edu.ruc.platform.admin.domain.LatestAuditImportJob;
import edu.ruc.platform.admin.domain.LatestSysOperationLog;
import edu.ruc.platform.admin.repository.DataImportErrorItemRepository;
import edu.ruc.platform.admin.repository.KnowledgeAttachmentRepository;
import edu.ruc.platform.admin.repository.AdminOperationLogRepository;
import edu.ruc.platform.admin.repository.DataImportTaskRepository;
import edu.ruc.platform.admin.repository.LatestAuditImportJobRepository;
import edu.ruc.platform.admin.repository.LatestSysOperationLogRepository;
import edu.ruc.platform.auth.domain.LatestUser;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.repository.LatestUserRepository;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.DataImportTaskStatus;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.knowledge.domain.KnowledgeDocument;
import edu.ruc.platform.knowledge.domain.LatestFileObject;
import edu.ruc.platform.knowledge.domain.LatestKnowledgePolicy;
import edu.ruc.platform.certificate.repository.CertificateRequestRepository;
import edu.ruc.platform.knowledge.repository.KnowledgeDocumentRepository;
import edu.ruc.platform.knowledge.repository.LatestFileObjectRepository;
import edu.ruc.platform.knowledge.repository.LatestKnowledgePolicyRepository;
import edu.ruc.platform.notice.domain.LatestNoticeDelivery;
import edu.ruc.platform.notice.domain.Notice;
import edu.ruc.platform.notice.domain.LatestNoticeItem;
import edu.ruc.platform.notice.domain.LatestNoticeItemTag;
import edu.ruc.platform.notice.domain.LatestNoticeTagDict;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.notice.repository.LatestNoticeDeliveryRepository;
import edu.ruc.platform.notice.repository.LatestNoticeItemRepository;
import edu.ruc.platform.notice.repository.LatestNoticeItemTagRepository;
import edu.ruc.platform.notice.repository.LatestNoticeTagDictRepository;
import edu.ruc.platform.notice.repository.NoticeRepository;
import edu.ruc.platform.student.domain.AdvisorScopeBinding;
import edu.ruc.platform.student.repository.AdvisorScopeBindingRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final LatestNoticeItemRepository latestNoticeItemRepository;
    private final LatestNoticeItemTagRepository latestNoticeItemTagRepository;
    private final LatestNoticeTagDictRepository latestNoticeTagDictRepository;
    private final LatestNoticeDeliveryRepository latestNoticeDeliveryRepository;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final LatestKnowledgePolicyRepository latestKnowledgePolicyRepository;
    private final LatestFileObjectRepository latestFileObjectRepository;
    private final LatestAuditImportJobRepository latestAuditImportJobRepository;
    private final LatestSysOperationLogRepository latestSysOperationLogRepository;
    private final LatestUserRepository latestUserRepository;

    @Override
    public List<TargetedNoticeResponse> listNotices() {
        if (isKingbaseProfile()) {
            return listLatestNotices();
        }
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
        List<TargetedNoticeResponse> filtered = isKingbaseProfile() ? filterLatestNotices(request) : filterNotices(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdminNoticeStatsResponse noticeStats(AdminNoticeFilterRequest request) {
        if (isKingbaseProfile()) {
            List<LatestAdminNoticeView> filtered = filterLatestNoticeViews(request);
            return new AdminNoticeStatsResponse(
                    filtered.size(),
                    (int) filtered.stream().filter(item -> !item.tags().isEmpty()).count(),
                    (int) filtered.stream().filter(item -> item.targetDescription() != null && item.targetDescription().contains("级")).count(),
                    (int) filtered.stream().filter(item -> item.targetDescription() != null && item.targetDescription().contains("毕业")).count()
            );
        }
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
        if (isKingbaseProfile()) {
            return createLatestNotice(request);
        }
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
        if (isKingbaseProfile()) {
            return listLatestKnowledgeItems(user);
        }
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
        List<AdminKnowledgeItemResponse> filtered = isKingbaseProfile()
                ? filterLatestKnowledgeItems(user, request)
                : filterKnowledgeItems(user, request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public AdminKnowledgeStatsResponse knowledgeStats(AuthenticatedUser user, AdminKnowledgeFilterRequest request) {
        List<AdminKnowledgeItemResponse> filtered = isKingbaseProfile()
                ? filterLatestKnowledgeItems(user, request)
                : filterKnowledgeItems(user, request);
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
        long totalAttachments = isKingbaseProfile()
                ? filtered.stream().filter(item -> item.sourceFileName() != null && !item.sourceFileName().isBlank()).count()
                : filtered.stream()
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
        if (isKingbaseProfile()) {
            return createLatestKnowledgeItem(request);
        }
        KnowledgeDocument item = new KnowledgeDocument();
        populateKnowledgeItem(item, request);
        item = knowledgeDocumentRepository.save(item);
        writeOperationLog("KNOWLEDGE", "CREATE", item.getTitle(), "SUCCESS", request.sourceFileName());
        return toKnowledgeResponse(item);
    }

    @Override
    public AdminKnowledgeItemResponse updateKnowledgeItem(Long id, AdminKnowledgeUpsertRequest request) {
        if (isKingbaseProfile()) {
            return updateLatestKnowledgeItem(id, request);
        }
        KnowledgeDocument item = knowledgeDocumentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识条目不存在"));
        populateKnowledgeItem(item, request);
        item = knowledgeDocumentRepository.save(item);
        writeOperationLog("KNOWLEDGE", "UPDATE", item.getTitle(), "SUCCESS", request.sourceFileName());
        return toKnowledgeResponse(item);
    }

    @Override
    public List<KnowledgeAttachmentResponse> listKnowledgeAttachments(Long knowledgeId) {
        if (isKingbaseProfile()) {
            LatestKnowledgePolicy item = latestKnowledgePolicyRepository.findById(knowledgeId)
                    .orElseThrow(() -> new BusinessException("知识条目不存在"));
            if (item.getAttachmentFileId() == null) {
                return List.of();
            }
            LatestFileObject fileObject = latestFileObjectRepository.findById(item.getAttachmentFileId())
                    .orElseThrow(() -> new BusinessException("附件不存在"));
            return List.of(toLatestKnowledgeAttachmentResponse(knowledgeId, fileObject));
        }
        if (!knowledgeDocumentRepository.existsById(knowledgeId)) {
            throw new BusinessException("知识条目不存在");
        }
        return knowledgeAttachmentRepository.findByKnowledgeIdOrderByCreatedAtDesc(knowledgeId).stream()
                .map(this::toKnowledgeAttachmentResponse)
                .toList();
    }

    @Override
    public KnowledgeAttachmentResponse uploadKnowledgeAttachment(Long knowledgeId, MultipartFile file) {
        if (isKingbaseProfile()) {
            return uploadLatestKnowledgeAttachment(knowledgeId, file);
        }
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
        if (isKingbaseProfile()) {
            deleteLatestKnowledgeAttachment(attachmentId);
            return;
        }
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
        if (isKingbaseProfile()) {
            return new AdminStatsResponse(
                    Math.toIntExact(latestUserRepository.countByIsDeleted(0)),
                    pendingCount,
                    Math.toIntExact(latestNoticeItemRepository.countByIsDeleted(0)),
                    Math.toIntExact(latestKnowledgePolicyRepository.countByIsDeletedAndIsPublished(0, 1))
            );
        }
        return new AdminStatsResponse(
                Math.toIntExact(studentProfileRepository.count()),
                pendingCount,
                Math.toIntExact(noticeRepository.count()),
                Math.toIntExact(knowledgeDocumentRepository.countByPublishedTrue())
        );
    }

    @Override
    public List<AdminOperationLogResponse> listOperationLogs() {
        if (isKingbaseProfile()) {
            return latestSysOperationLogRepository.findAll().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .map(this::toLatestOperationLogResponse)
                    .toList();
        }
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
        if (isKingbaseProfile()) {
            return latestAuditImportJobRepository.findAll().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .map(this::toLatestImportTaskResponse)
                    .toList();
        }
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
        if (isKingbaseProfile()) {
            AuthenticatedUser user = currentUserService.requireCurrentUser();
            LatestFileObject sourceFile = createImportSourceFile(request.fileName(), user.userId());
            LatestAuditImportJob job = new LatestAuditImportJob();
            job.setJobType(request.taskType());
            job.setSourceFileId(sourceFile.getId());
            job.setStatus("running");
            job.setTotalRows(request.totalRows());
            job.setSuccessRows(0);
            job.setFailedRows(0);
            job.setErrorMessage(null);
            job.setStartedBy(user.userId());
            job.setStartedAt(LocalDateTime.now());
            job = latestAuditImportJobRepository.save(job);
            job.setResultJson(buildImportJobResultJson(job, "CREATED", request.owner(), sourceFile, List.of()));
            job = latestAuditImportJobRepository.save(job);
            writeOperationLog("IMPORT_TASK", "CREATE", request.fileName(), "SUCCESS", request.taskType());
            return toLatestImportTaskResponse(job);
        }
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
        if (isKingbaseProfile()) {
            LatestAuditImportJob job = latestAuditImportJobRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("导入任务不存在"));
            validateImportTaskRows(defaultZero(job.getTotalRows()), request.successRows(), request.failedRows());
            validateImportTaskTransition(toLatestImportTaskResponse(job), request);
            job.setStatus(mapImportJobStatus(request.status()));
            job.setSuccessRows(request.successRows());
            job.setFailedRows(request.failedRows());
            job.setErrorMessage(request.errorSummary());
            if (!"RUNNING".equals(request.status()) && !"CREATED".equals(request.status())) {
                job.setFinishedAt(LocalDateTime.now());
            } else {
                job.setFinishedAt(null);
            }
            LatestFileObject sourceFile = latestFileObjectRepository.findById(job.getSourceFileId()).orElse(null);
            List<DataImportErrorItemResponse> errors = readLatestImportErrors(job);
            String ownerName = parseLatestImportJobMeta(job.getResultJson())
                    .getOrDefault("ownerName", resolveOperatorName(job.getStartedBy()));
            job.setResultJson(buildImportJobResultJson(job, request.status(), ownerName, sourceFile, errors));
            job = latestAuditImportJobRepository.save(job);
            writeOperationLog("IMPORT_TASK", "UPDATE", toLatestImportTaskResponse(job).fileName(), request.status(), request.errorSummary());
            return toLatestImportTaskResponse(job);
        }
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
        if (isKingbaseProfile()) {
            LatestAuditImportJob job = latestAuditImportJobRepository.findById(taskId)
                    .orElseThrow(() -> new BusinessException("导入任务不存在"));
            return readLatestImportErrors(job);
        }
        if (!dataImportTaskRepository.existsById(taskId)) {
            throw new BusinessException("导入任务不存在");
        }
        return dataImportErrorItemRepository.findByTaskIdOrderByRowNumberAscCreatedAtAsc(taskId).stream()
                .map(this::toImportErrorResponse)
                .toList();
    }

    @Override
    public PageResponse<DataImportErrorItemResponse> pageImportErrors(Long taskId, DataImportErrorFilterRequest request, int page, int size) {
        List<DataImportErrorItemResponse> filtered = isKingbaseProfile()
                ? filterLatestImportErrors(taskId, request)
                : filterImportErrors(taskId, request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public DataImportErrorItemResponse createImportError(Long taskId, DataImportErrorItemCreateRequest request) {
        if (isKingbaseProfile()) {
            LatestAuditImportJob job = latestAuditImportJobRepository.findById(taskId)
                    .orElseThrow(() -> new BusinessException("导入任务不存在"));
            List<DataImportErrorItemResponse> errors = new java.util.ArrayList<>(readLatestImportErrors(job));
            DataImportErrorItemResponse created = new DataImportErrorItemResponse(
                    System.currentTimeMillis(),
                    taskId,
                    request.rowNumber(),
                    request.fieldName(),
                    request.errorMessage(),
                    request.rawValue(),
                    LocalDateTime.now()
            );
            errors.add(created);
            errors.sort(java.util.Comparator
                    .comparing(DataImportErrorItemResponse::rowNumber)
                    .thenComparing(DataImportErrorItemResponse::createdAt));
            LatestFileObject sourceFile = latestFileObjectRepository.findById(job.getSourceFileId()).orElse(null);
            String appStatus = parseLatestImportJobMeta(job.getResultJson())
                    .getOrDefault("appStatus", deriveImportTaskStatus(job.getStatus(), job.getSuccessRows(), job.getFailedRows()));
            String ownerName = parseLatestImportJobMeta(job.getResultJson())
                    .getOrDefault("ownerName", resolveOperatorName(job.getStartedBy()));
            job.setResultJson(buildImportJobResultJson(job, appStatus, ownerName, sourceFile, errors));
            latestAuditImportJobRepository.save(job);
            writeOperationLog("IMPORT_TASK", "ADD_ERROR", toLatestImportTaskResponse(job).fileName(), "SUCCESS", "row=" + request.rowNumber());
            return created;
        }
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

    @Override
    public void replaceImportErrors(Long taskId, List<DataImportErrorItemCreateRequest> requests) {
        List<DataImportErrorItemCreateRequest> safeRequests = requests == null ? List.of() : requests;
        if (isKingbaseProfile()) {
            LatestAuditImportJob job = latestAuditImportJobRepository.findById(taskId)
                    .orElseThrow(() -> new BusinessException("导入任务不存在"));
            DataImportTaskResponse task = toLatestImportTaskResponse(job);
            List<DataImportErrorItemResponse> errors = new java.util.ArrayList<>();
            for (DataImportErrorItemCreateRequest request : safeRequests) {
                validateImportErrorRequest(task, request);
                errors.add(new DataImportErrorItemResponse(
                        System.currentTimeMillis() + errors.size(),
                        taskId,
                        request.rowNumber(),
                        request.fieldName(),
                        request.errorMessage(),
                        request.rawValue(),
                        LocalDateTime.now()
                ));
            }
            LatestFileObject sourceFile = latestFileObjectRepository.findById(job.getSourceFileId()).orElse(null);
            String appStatus = parseLatestImportJobMeta(job.getResultJson())
                    .getOrDefault("appStatus", deriveImportTaskStatus(job.getStatus(), job.getSuccessRows(), job.getFailedRows()));
            String ownerName = parseLatestImportJobMeta(job.getResultJson())
                    .getOrDefault("ownerName", resolveOperatorName(job.getStartedBy()));
            job.setResultJson(buildImportJobResultJson(job, appStatus, ownerName, sourceFile, errors));
            latestAuditImportJobRepository.save(job);
            writeOperationLog("IMPORT_TASK", "REPLACE_ERRORS", task.fileName(), "SUCCESS", "count=" + errors.size());
            return;
        }
        DataImportTask task = dataImportTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        dataImportErrorItemRepository.deleteByTaskId(taskId);
        for (DataImportErrorItemCreateRequest request : safeRequests) {
            validateImportErrorRequest(toImportTaskResponse(task), request);
            DataImportErrorItem item = new DataImportErrorItem();
            item.setTaskId(taskId);
            item.setRowNumber(request.rowNumber());
            item.setFieldName(request.fieldName());
            item.setErrorMessage(request.errorMessage());
            item.setRawValue(request.rawValue());
            dataImportErrorItemRepository.save(item);
        }
        writeOperationLog("IMPORT_TASK", "REPLACE_ERRORS", task.getFileName(), "SUCCESS", "count=" + safeRequests.size());
    }

    @Override
    public void recordImportExecutionContext(Long taskId, String executionBatchNo, String callbackSource) {
        if (!isKingbaseProfile()) {
            return;
        }
        LatestAuditImportJob job = latestAuditImportJobRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        LatestFileObject sourceFile = latestFileObjectRepository.findById(job.getSourceFileId()).orElse(null);
        List<DataImportErrorItemResponse> errors = readLatestImportErrors(job);
        String appStatus = parseLatestImportJobMeta(job.getResultJson())
                .getOrDefault("appStatus", deriveImportTaskStatus(job.getStatus(), job.getSuccessRows(), job.getFailedRows()));
        String ownerName = parseLatestImportJobMeta(job.getResultJson())
                .getOrDefault("ownerName", resolveOperatorName(job.getStartedBy()));
        job.setResultJson(buildImportJobResultJson(job, appStatus, ownerName, sourceFile, errors, executionBatchNo, callbackSource));
        latestAuditImportJobRepository.save(job);
    }

    @Override
    public ImportExecutionContext getImportExecutionContext(Long taskId) {
        if (!isKingbaseProfile()) {
            return null;
        }
        LatestAuditImportJob job = latestAuditImportJobRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("导入任务不存在"));
        Map<String, Object> payload = parseLatestImportJobPayload(job.getResultJson());
        String executionBatchNo = stringValue(payload.get("executionBatchNo"));
        String callbackSource = stringValue(payload.get("callbackSource"));
        String lastExecutedAt = stringValue(payload.get("lastExecutedAt"));
        if (executionBatchNo == null && callbackSource == null && lastExecutedAt == null) {
            return null;
        }
        return new ImportExecutionContext(executionBatchNo, callbackSource, parseLocalDateTime(lastExecutedAt));
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

    private DataImportTaskResponse toLatestImportTaskResponse(LatestAuditImportJob job) {
        LatestFileObject sourceFile = latestFileObjectRepository.findById(job.getSourceFileId()).orElse(null);
        Map<String, String> meta = parseLatestImportJobMeta(job.getResultJson());
        String fileName = sourceFile == null ? "unknown-file" : sourceFile.getOriginalName();
        return new DataImportTaskResponse(
                job.getId(),
                job.getJobType(),
                fileName,
                resolveFileType(fileName),
                resolveTemplateName(job.getJobType()),
                resolveTemplateDownloadUrl(job.getJobType()),
                meta.getOrDefault("appStatus", deriveImportTaskStatus(job.getStatus(), job.getSuccessRows(), job.getFailedRows())),
                defaultZero(job.getTotalRows()),
                defaultZero(job.getSuccessRows()),
                defaultZero(job.getFailedRows()),
                calculateProgressPercent(defaultZero(job.getTotalRows()), defaultZero(job.getSuccessRows()), defaultZero(job.getFailedRows())),
                meta.getOrDefault("ownerName", resolveOperatorName(job.getStartedBy())),
                job.getCreatedAt()
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

    private AdminOperationLogResponse toLatestOperationLogResponse(LatestSysOperationLog log) {
        Map<String, String> meta = parseOperationLogMeta(log.getExtJson());
        return new AdminOperationLogResponse(
                log.getId(),
                log.getOperatorUserId(),
                meta.getOrDefault("operatorName", resolveOperatorName(log.getOperatorUserId())),
                meta.getOrDefault("operatorRole", null),
                log.getModuleCode() == null ? null : log.getModuleCode().toUpperCase(java.util.Locale.ROOT),
                log.getOperationType() == null ? null : log.getOperationType().toUpperCase(java.util.Locale.ROOT),
                meta.getOrDefault("target", log.getBusinessType() == null ? "unknown" : log.getBusinessType() + "#" + log.getBusinessId()),
                mapOperationResult(log.getResultStatus()),
                log.getOperationDesc(),
                log.getCreatedAt()
        );
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

    private List<AdminKnowledgeItemResponse> listLatestKnowledgeItems(AuthenticatedUser user) {
        return latestKnowledgePolicyRepository.findByIsDeletedOrderByUpdatedAtDesc(0).stream()
                .map(this::toLatestKnowledgeResponse)
                .filter(item -> canViewLatestKnowledgeItem(user, item))
                .toList();
    }

    private List<AdminKnowledgeItemResponse> filterLatestKnowledgeItems(AuthenticatedUser user, AdminKnowledgeFilterRequest request) {
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        String normalizedCategory = QueryFilterSupport.trimToNull(request.category());
        return listLatestKnowledgeItems(user).stream()
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

    private KnowledgeAttachmentResponse toLatestKnowledgeAttachmentResponse(Long knowledgeId, LatestFileObject fileObject) {
        return new KnowledgeAttachmentResponse(
                fileObject.getId(),
                knowledgeId,
                fileObject.getOriginalName(),
                fileObject.getMimeType(),
                fileObject.getSizeBytes(),
                fileObject.getStoragePath(),
                fileObject.getUploadedBy() == null ? "system" : "user#" + fileObject.getUploadedBy(),
                fileObject.getUploadedAt()
        );
    }

    private AdminKnowledgeItemResponse toLatestKnowledgeResponse(LatestKnowledgePolicy item) {
        Map<String, String> meta = parseLatestKnowledgeMeta(item.getExtJson());
        String sourceFileName = null;
        if (item.getAttachmentFileId() != null) {
            sourceFileName = latestFileObjectRepository.findById(item.getAttachmentFileId())
                    .map(LatestFileObject::getOriginalName)
                    .orElse(meta.get("sourceFileName"));
        }
        if (sourceFileName == null) {
            sourceFileName = meta.get("sourceFileName");
        }
        return new AdminKnowledgeItemResponse(
                item.getId(),
                item.getTitle(),
                meta.getOrDefault("category", "未分类"),
                item.getIsPublished() != null && item.getIsPublished() == 1,
                item.getSourceUrl(),
                sourceFileName,
                meta.get("audienceScope"),
                meta.get("updatedBy")
        );
    }

    private Map<String, String> parseLatestKnowledgeMeta(String extJson) {
        if (extJson == null || extJson.isBlank()) {
            return java.util.Map.of();
        }
        try {
            return objectMapper.readValue(extJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return java.util.Map.of();
        }
    }

    private Map<String, String> parseLatestImportJobMeta(String resultJson) {
        return parseLatestImportJobPayload(resultJson).entrySet().stream()
                .filter(entry -> entry.getValue() instanceof String)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue())));
    }

    private Map<String, String> parseOperationLogMeta(String extJson) {
        if (extJson == null || extJson.isBlank()) {
            return java.util.Map.of();
        }
        try {
            return objectMapper.readValue(extJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return java.util.Map.of();
        }
    }

    private boolean canViewLatestKnowledgeItem(AuthenticatedUser user, AdminKnowledgeItemResponse item) {
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

    private AdminKnowledgeItemResponse createLatestKnowledgeItem(AdminKnowledgeUpsertRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        LatestKnowledgePolicy item = new LatestKnowledgePolicy();
        populateLatestKnowledgeItem(item, request, user);
        item = latestKnowledgePolicyRepository.save(item);
        writeOperationLog("KNOWLEDGE", "CREATE", item.getTitle(), "SUCCESS", request.sourceFileName());
        return toLatestKnowledgeResponse(item);
    }

    private AdminKnowledgeItemResponse updateLatestKnowledgeItem(Long id, AdminKnowledgeUpsertRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        LatestKnowledgePolicy item = latestKnowledgePolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("知识条目不存在"));
        populateLatestKnowledgeItem(item, request, user);
        item = latestKnowledgePolicyRepository.save(item);
        writeOperationLog("KNOWLEDGE", "UPDATE", item.getTitle(), "SUCCESS", request.sourceFileName());
        return toLatestKnowledgeResponse(item);
    }

    private void populateLatestKnowledgeItem(LatestKnowledgePolicy item,
                                             AdminKnowledgeUpsertRequest request,
                                             AuthenticatedUser user) {
        item.setTitle(request.title());
        item.setSummary(buildKnowledgeSummary(request.content()));
        item.setContent(request.content());
        item.setSourceType(request.sourceFileName() == null || request.sourceFileName().isBlank() ? "manual" : "import");
        item.setSourceUrl(request.officialUrl());
        item.setIsPublished(Boolean.TRUE.equals(request.published()) ? 1 : 0);
        item.setPublishedAt(Boolean.TRUE.equals(request.published()) ? LocalDateTime.now() : null);
        item.setCreatedBy(user.userId());
        item.setExtJson(buildLatestKnowledgeMetaJson(request));
        item.setIsDeleted(0);
    }

    private String buildKnowledgeSummary(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        return content.length() > 80 ? content.substring(0, 80) : content;
    }

    private String buildLatestKnowledgeMetaJson(AdminKnowledgeUpsertRequest request) {
        Map<String, String> meta = new java.util.LinkedHashMap<>();
        meta.put("category", request.category());
        meta.put("sourceFileName", request.sourceFileName());
        meta.put("audienceScope", request.audienceScope());
        meta.put("updatedBy", request.updatedBy());
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private LatestFileObject createImportSourceFile(String fileName, Long userId) {
        LatestFileObject fileObject = new LatestFileObject();
        fileObject.setPurpose("import_source");
        fileObject.setOriginalName(fileName);
        fileObject.setMimeType("application/octet-stream");
        fileObject.setSizeBytes(0L);
        fileObject.setStorageProvider("local");
        fileObject.setStoragePath("/uploads/import/" + System.currentTimeMillis() + "-" + fileName);
        fileObject.setUploadedBy(userId);
        fileObject.setUploadedAt(LocalDateTime.now());
        fileObject.setIsDeleted(0);
        return latestFileObjectRepository.save(fileObject);
    }

    private String buildImportJobResultJson(LatestAuditImportJob job,
                                            String appStatus,
                                            String ownerName,
                                            LatestFileObject sourceFile,
                                            List<DataImportErrorItemResponse> errors) {
        Map<String, Object> existing = parseLatestImportJobPayload(job.getResultJson());
        return buildImportJobResultJson(
                job,
                appStatus,
                ownerName,
                sourceFile,
                errors,
                stringValue(existing.get("executionBatchNo")),
                stringValue(existing.get("callbackSource"))
        );
    }

    private String buildImportJobResultJson(LatestAuditImportJob job,
                                            String appStatus,
                                            String ownerName,
                                            LatestFileObject sourceFile,
                                            List<DataImportErrorItemResponse> errors,
                                            String executionBatchNo,
                                            String callbackSource) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        Map<String, Object> existing = parseLatestImportJobPayload(job.getResultJson());
        payload.put("appStatus", appStatus);
        payload.put("ownerName", ownerName);
        payload.put("ownerUserId", job.getStartedBy());
        payload.put("sourceFileId", job.getSourceFileId());
        payload.put("sourceFileName", sourceFile == null ? null : sourceFile.getOriginalName());
        payload.put("fileType", sourceFile == null ? "unknown" : resolveFileType(sourceFile.getOriginalName()));
        payload.put("templateName", resolveTemplateName(job.getJobType()));
        payload.put("templateDownloadUrl", resolveTemplateDownloadUrl(job.getJobType()));
        payload.put("totalRows", defaultZero(job.getTotalRows()));
        payload.put("successRows", defaultZero(job.getSuccessRows()));
        payload.put("failedRows", defaultZero(job.getFailedRows()));
        payload.put("progressPercent", calculateProgressPercent(defaultZero(job.getTotalRows()), defaultZero(job.getSuccessRows()), defaultZero(job.getFailedRows())));
        payload.put("errorSummary", job.getErrorMessage());
        payload.put("errorCount", errors.size());
        payload.put("pendingErrorResolution", defaultZero(job.getFailedRows()) > 0
                && ("PARTIAL_SUCCESS".equals(appStatus) || "FAILED".equals(appStatus)));
        payload.put("startedAt", job.getStartedAt() == null ? null : job.getStartedAt().toString());
        payload.put("finishedAt", job.getFinishedAt() == null ? null : job.getFinishedAt().toString());
        payload.put("receiptCode", existing.getOrDefault("receiptCode",
                "IMP-" + job.getId() + "-" + job.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
        payload.put("receiptGeneratedAt", existing.getOrDefault("receiptGeneratedAt",
                (job.getCreatedAt() == null ? LocalDateTime.now() : job.getCreatedAt()).toString()));
        payload.put("executionBatchNo", executionBatchNo);
        payload.put("callbackSource", callbackSource);
        payload.put("lastExecutedAt", executionBatchNo == null && callbackSource == null
                ? existing.get("lastExecutedAt")
                : LocalDateTime.now().toString());
        payload.put("updatedAt", LocalDateTime.now().toString());
        payload.put("errors", errors.stream().map(this::toImportErrorPayload).toList());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{\"appStatus\":\"" + appStatus + "\"}";
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private LocalDateTime parseLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private String mapImportJobStatus(String appStatus) {
        return switch (DataImportTaskStatus.from(appStatus)) {
            case CREATED -> "created";
            case RUNNING -> "running";
            case SUCCESS, PARTIAL_SUCCESS -> "done";
            case FAILED -> "failed";
        };
    }

    private String deriveImportTaskStatus(String latestStatus, Integer successRows, Integer failedRows) {
        if ("created".equalsIgnoreCase(latestStatus)) {
            return "CREATED";
        }
        if ("failed".equalsIgnoreCase(latestStatus)) {
            return "FAILED";
        }
        if ("done".equalsIgnoreCase(latestStatus)) {
            return defaultZero(failedRows) > 0 ? "PARTIAL_SUCCESS" : "SUCCESS";
        }
        return "RUNNING";
    }

    private String resolveOperatorName(Long userId) {
        if (userId == null) {
            return "system";
        }
        return latestUserRepository.findById(userId)
                .map(LatestUser::getFullName)
                .orElse("user#" + userId);
    }

    private String mapOperationResult(String resultStatus) {
        if (resultStatus == null) {
            return "UNKNOWN";
        }
        return switch (resultStatus.toLowerCase(java.util.Locale.ROOT)) {
            case "success" -> "SUCCESS";
            case "fail" -> "FAILED";
            case "partial" -> "PARTIAL_SUCCESS";
            default -> resultStatus.toUpperCase(java.util.Locale.ROOT);
        };
    }

    private KnowledgeAttachmentResponse uploadLatestKnowledgeAttachment(Long knowledgeId, MultipartFile file) {
        LatestKnowledgePolicy knowledge = latestKnowledgePolicyRepository.findById(knowledgeId)
                .orElseThrow(() -> new BusinessException("知识条目不存在"));
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        long maxSize = 30L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("知识附件大小不能超过 30MB");
        }
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        LatestFileObject fileObject = new LatestFileObject();
        fileObject.setPurpose("knowledge_attachment");
        fileObject.setOriginalName(file.getOriginalFilename() == null ? "unknown-file" : file.getOriginalFilename());
        fileObject.setMimeType(file.getContentType());
        fileObject.setSizeBytes(file.getSize());
        fileObject.setStorageProvider("local");
        fileObject.setStoragePath("/uploads/knowledge/" + knowledgeId + "/" + System.currentTimeMillis() + "-" + fileObject.getOriginalName());
        fileObject.setUploadedBy(user.userId());
        fileObject.setUploadedAt(LocalDateTime.now());
        fileObject.setIsDeleted(0);
        fileObject = latestFileObjectRepository.save(fileObject);
        knowledge.setAttachmentFileId(fileObject.getId());
        latestKnowledgePolicyRepository.save(knowledge);
        writeOperationLog("KNOWLEDGE_ATTACHMENT", "UPLOAD", knowledge.getTitle(), "SUCCESS", fileObject.getOriginalName());
        return toLatestKnowledgeAttachmentResponse(knowledgeId, fileObject);
    }

    private void deleteLatestKnowledgeAttachment(Long attachmentId) {
        LatestFileObject fileObject = latestFileObjectRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("知识附件不存在"));
        latestKnowledgePolicyRepository.findByIsDeletedOrderByUpdatedAtDesc(0).stream()
                .filter(item -> java.util.Objects.equals(item.getAttachmentFileId(), attachmentId))
                .findFirst()
                .ifPresent(item -> {
                    item.setAttachmentFileId(null);
                    latestKnowledgePolicyRepository.save(item);
                });
        fileObject.setIsDeleted(1);
        latestFileObjectRepository.save(fileObject);
        writeOperationLog("KNOWLEDGE_ATTACHMENT", "DELETE", "attachment#" + attachmentId, "SUCCESS", fileObject.getOriginalName());
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

    private List<TargetedNoticeResponse> listLatestNotices() {
        return filterLatestNotices(new AdminNoticeFilterRequest(null, null, null));
    }

    private List<TargetedNoticeResponse> filterLatestNotices(AdminNoticeFilterRequest request) {
        return filterLatestNoticeViews(request).stream()
                .map(this::toLatestNoticeResponse)
                .toList();
    }

    private List<LatestAdminNoticeView> filterLatestNoticeViews(AdminNoticeFilterRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        String normalizedTag = QueryFilterSupport.trimToNull(request.tag());
        String normalizedTargetKeyword = QueryFilterSupport.trimToNull(request.targetKeyword());
        return latestNoticeItemRepository.findByIsDeletedOrderByPublishAtDesc(0).stream()
                .map(this::buildLatestNoticeView)
                .filter(item -> canViewLatestNotice(user, item))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.title(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.summary(), normalizedKeyword))
                .filter(item -> normalizedTag == null
                        || item.tags().stream().anyMatch(tag -> tag.equalsIgnoreCase(normalizedTag)))
                .filter(item -> normalizedTargetKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.targetDescription(), normalizedTargetKeyword))
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

    private boolean canViewLatestNotice(AuthenticatedUser user, LatestAdminNoticeView notice) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return true;
        }
        if (!"CLASS_ADVISOR".equals(user.role())) {
            return false;
        }
        String targetDescription = notice.targetDescription();
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

    private LatestAdminNoticeView buildLatestNoticeView(LatestNoticeItem item) {
        List<String> tags = latestNoticeItemTagRepository.findByNoticeId(item.getId()).stream()
                .map(LatestNoticeItemTag::getTagId)
                .map(id -> latestNoticeTagDictRepository.findById(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(LatestNoticeTagDict::getTagName)
                .toList();
        List<LatestNoticeDelivery> deliveries = latestNoticeDeliveryRepository.findByNoticeId(item.getId());
        NoticeTargetScope scope = resolveLatestTargetScope(deliveries);
        return new LatestAdminNoticeView(
                item.getId(),
                item.getTitle(),
                summarizeLatestNotice(item),
                tags,
                buildLatestTargetDescription(scope),
                resolveLatestPriority(scope, tags),
                resolveLatestMatchedRules(scope, tags),
                resolveLatestDeliveryChannels(deliveries, tags),
                item.getPublishAt() == null ? item.getCreatedAt() : item.getPublishAt()
        );
    }

    private String summarizeLatestNotice(LatestNoticeItem item) {
        if (item.getContent() == null || item.getContent().isBlank()) {
            return item.getTitle();
        }
        return item.getContent().length() > 80 ? item.getContent().substring(0, 80) : item.getContent();
    }

    private NoticeTargetScope resolveLatestTargetScope(List<LatestNoticeDelivery> deliveries) {
        Integer gradeYear = null;
        String major = null;
        boolean graduatedOnly = false;
        boolean scholarshipCandidates = false;
        for (LatestNoticeDelivery delivery : deliveries) {
            if (delivery.getTargetRuleJson() == null || delivery.getTargetRuleJson().isBlank()) {
                continue;
            }
            try {
                java.util.Map<String, Object> rule = objectMapper.readValue(delivery.getTargetRuleJson(), new TypeReference<>() {
                });
                if (gradeYear == null && rule.get("gradeYears") instanceof List<?> gradeYears && !gradeYears.isEmpty()) {
                    gradeYear = Integer.valueOf(String.valueOf(gradeYears.get(0)));
                }
                if (major == null && rule.get("majors") instanceof List<?> majors && !majors.isEmpty()) {
                    major = String.valueOf(majors.get(0));
                }
                if (rule.get("graduatedOnly") instanceof Boolean only && only) {
                    graduatedOnly = true;
                }
                if (rule.get("scholarshipCandidates") instanceof Boolean only && only) {
                    scholarshipCandidates = true;
                }
            } catch (Exception ignored) {
            }
        }
        return new NoticeTargetScope(gradeYear, major, graduatedOnly, scholarshipCandidates);
    }

    private String buildLatestTargetDescription(NoticeTargetScope scope) {
        if (scope.graduatedOnly()) {
            return "毕业生";
        }
        String grade = scope.gradeYear() == null ? null : scope.gradeYear() + "级";
        if (grade != null && scope.major() != null) {
            return grade + "/" + scope.major();
        }
        if (grade != null) {
            return grade;
        }
        if (scope.major() != null) {
            return scope.major();
        }
        if (scope.scholarshipCandidates()) {
            return "奖学金候选学生";
        }
        return "全体学生";
    }

    private String resolveLatestPriority(NoticeTargetScope scope, List<String> tags) {
        if (scope.gradeYear() != null || scope.major() != null || scope.scholarshipCandidates()) {
            return "HIGH";
        }
        if (tags.stream().anyMatch(tag -> tag.contains("流程") || tag.contains("党") || tag.contains("团"))) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> resolveLatestMatchedRules(NoticeTargetScope scope, List<String> tags) {
        List<String> rules = new java.util.ArrayList<>();
        if (scope.gradeYear() == null && scope.major() == null && !scope.graduatedOnly() && !scope.scholarshipCandidates()) {
            rules.add("全体学生");
        }
        if (scope.gradeYear() != null) {
            rules.add("年级匹配");
        }
        if (scope.major() != null) {
            rules.add("专业匹配");
        }
        if (scope.graduatedOnly()) {
            rules.add("毕业生匹配");
        }
        if (scope.scholarshipCandidates()) {
            rules.add("奖学金候选");
        }
        if (tags.stream().anyMatch(tag -> tag.contains("就业") || tag.contains("实习"))) {
            rules.add("就业标签");
        }
        if (tags.stream().anyMatch(tag -> tag.contains("流程") || tag.contains("党") || tag.contains("团"))) {
            rules.add("党团事务标签");
        }
        return rules;
    }

    private List<String> resolveLatestDeliveryChannels(List<LatestNoticeDelivery> deliveries, List<String> tags) {
        List<String> channels = deliveries.stream()
                .map(LatestNoticeDelivery::getChannel)
                .filter(java.util.Objects::nonNull)
                .map(this::mapLatestChannel)
                .distinct()
                .toList();
        if (!channels.isEmpty()) {
            return channels;
        }
        if (tags.stream().anyMatch(tag -> tag.contains("就业") || tag.contains("实习"))) {
            return List.of("IN_APP", "EMAIL", "WECHAT");
        }
        if (tags.stream().anyMatch(tag -> tag.contains("流程") || tag.contains("党") || tag.contains("团"))) {
            return List.of("IN_APP", "EMAIL");
        }
        return List.of("IN_APP");
    }

    private String mapLatestChannel(String channel) {
        return switch (channel) {
            case "email" -> "EMAIL";
            case "sms", "miniprogram" -> "WECHAT";
            default -> "IN_APP";
        };
    }

    private TargetedNoticeResponse toLatestNoticeResponse(LatestAdminNoticeView notice) {
        return new TargetedNoticeResponse(
                notice.id(),
                notice.title(),
                notice.summary(),
                notice.tags(),
                notice.targetDescription(),
                notice.priority(),
                notice.matchedRules(),
                notice.deliveryChannels(),
                notice.publishTime()
        );
    }

    private TargetedNoticeResponse createLatestNotice(AdminNoticeCreateRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        LatestNoticeItem item = new LatestNoticeItem();
        item.setTitle(request.title());
        item.setContent(request.summary());
        item.setSourceName(user.name());
        item.setPublishAt(LocalDateTime.now());
        item.setCreatedBy(user.userId());
        item.setExtJson("{}");
        item.setIsDeleted(0);
        item = latestNoticeItemRepository.save(item);

        for (String tag : request.tags()) {
            String normalizedTag = tag == null ? null : tag.trim();
            if (normalizedTag == null || normalizedTag.isEmpty()) {
                continue;
            }
            String tagCode = normalizedTag.toLowerCase(java.util.Locale.ROOT).replace(" ", "_");
            LatestNoticeTagDict dict = latestNoticeTagDictRepository.findByTagCode(tagCode)
                    .orElseGet(() -> {
                        LatestNoticeTagDict created = new LatestNoticeTagDict();
                        created.setTagCode(tagCode);
                        created.setTagName(normalizedTag);
                        created.setIsDeleted(0);
                        return latestNoticeTagDictRepository.save(created);
                    });
            LatestNoticeItemTag itemTag = new LatestNoticeItemTag();
            itemTag.setNoticeId(item.getId());
            itemTag.setTagId(dict.getId());
            itemTag.setCreatedAt(LocalDateTime.now());
            latestNoticeItemTagRepository.save(itemTag);
        }

        LatestNoticeDelivery delivery = new LatestNoticeDelivery();
        delivery.setNoticeId(item.getId());
        delivery.setChannel("miniprogram");
        delivery.setTargetRuleJson(buildLatestTargetRuleJson(request.targetDescription()));
        delivery.setStatus("done");
        delivery.setScheduledAt(item.getPublishAt());
        delivery.setSentAt(item.getPublishAt());
        delivery.setCreatedBy(user.userId());
        delivery.setExtJson("{}");
        latestNoticeDeliveryRepository.save(delivery);

        writeOperationLog("NOTICE", "CREATE", item.getTitle(), "SUCCESS", request.targetDescription());
        return toLatestNoticeResponse(buildLatestNoticeView(item));
    }

    private String buildLatestTargetRuleJson(String targetDescription) {
        String normalized = QueryFilterSupport.trimToNull(targetDescription);
        if (normalized == null || "全体学生".equals(normalized)) {
            return "{}";
        }
        java.util.Map<String, Object> rule = new java.util.LinkedHashMap<>();
        if ("毕业生".equals(normalized)) {
            rule.put("graduatedOnly", true);
        } else if (normalized.contains("/")) {
            String[] parts = normalized.split("/", 2);
            Integer gradeYear = parseGradeYear(parts[0]);
            if (gradeYear != null) {
                rule.put("gradeYears", List.of(gradeYear));
            }
            String major = QueryFilterSupport.trimToNull(parts[1]);
            if (major != null) {
                rule.put("majors", List.of(major));
            }
        } else {
            Integer gradeYear = parseGradeYear(normalized);
            if (gradeYear != null) {
                rule.put("gradeYears", List.of(gradeYear));
            } else {
                rule.put("majors", List.of(normalized));
            }
        }
        try {
            return objectMapper.writeValueAsString(rule);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private Integer parseGradeYear(String value) {
        String normalized = QueryFilterSupport.trimToNull(value);
        if (normalized == null) {
            return null;
        }
        String digits = normalized.replace("级", "").trim();
        if (!digits.matches("\\d{4}")) {
            return null;
        }
        return Integer.valueOf(digits);
    }

    private boolean isKingbaseProfile() {
        return environment.acceptsProfiles(Profiles.of("kingbase"));
    }

    private record LatestAdminNoticeView(Long id,
                                         String title,
                                         String summary,
                                         List<String> tags,
                                         String targetDescription,
                                         String priority,
                                         List<String> matchedRules,
                                         List<String> deliveryChannels,
                                         LocalDateTime publishTime) {
    }

    private record NoticeTargetScope(Integer gradeYear, String major, boolean graduatedOnly, boolean scholarshipCandidates) {
    }

    private void validateImportTaskRows(int totalRows, int successRows, int failedRows) {
        if (successRows + failedRows > totalRows) {
            throw new BusinessException("成功行数与失败行数之和不能超过总行数");
        }
    }

    private void validateImportErrorRequest(DataImportTaskResponse task, DataImportErrorItemCreateRequest request) {
        if (request.rowNumber() > task.totalRows()) {
            throw new BusinessException("错误行号不能超过导入任务总行数");
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
        if (isKingbaseProfile()) {
            LatestSysOperationLog log = new LatestSysOperationLog();
            log.setModuleCode(module.toLowerCase(java.util.Locale.ROOT));
            log.setBusinessType(module.toLowerCase(java.util.Locale.ROOT));
            log.setBusinessId(null);
            log.setOperationType(action.toLowerCase(java.util.Locale.ROOT));
            log.setOperationDesc(detail == null || detail.isBlank() ? target : detail);
            log.setOperatorUserId(user.userId());
            log.setTraceId("trace-" + module.toLowerCase(java.util.Locale.ROOT) + "-" + System.currentTimeMillis());
            log.setRequestUri(null);
            log.setRequestMethod(null);
            log.setRequestIp(null);
            log.setUserAgent(null);
            log.setLogLevel("audit");
            log.setResultStatus("SUCCESS".equalsIgnoreCase(result) ? "success" : "fail");
            log.setErrorMessage("SUCCESS".equalsIgnoreCase(result) ? null : detail);
            log.setExtJson(buildOperationLogExtJson(user, target));
            log.setCreatedAt(LocalDateTime.now());
            latestSysOperationLogRepository.save(log);
            return;
        }
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

    private String buildOperationLogExtJson(AuthenticatedUser user, String target) {
        Map<String, String> payload = new java.util.LinkedHashMap<>();
        payload.put("operatorName", user.name());
        payload.put("operatorRole", user.role());
        payload.put("target", target);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Map<String, Object> parseLatestImportJobPayload(String resultJson) {
        if (resultJson == null || resultJson.isBlank()) {
            return java.util.Map.of();
        }
        try {
            return objectMapper.readValue(resultJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return java.util.Map.of();
        }
    }

    private List<DataImportErrorItemResponse> readLatestImportErrors(LatestAuditImportJob job) {
        Object rawErrors = parseLatestImportJobPayload(job.getResultJson()).get("errors");
        if (!(rawErrors instanceof List<?> errors)) {
            return List.of();
        }
        return errors.stream()
                .map(this::toLatestImportErrorResponse)
                .filter(Objects::nonNull)
                .sorted(java.util.Comparator
                        .comparing(DataImportErrorItemResponse::rowNumber)
                        .thenComparing(DataImportErrorItemResponse::createdAt))
                .toList();
    }

    private DataImportErrorItemResponse toLatestImportErrorResponse(Object raw) {
        if (!(raw instanceof Map<?, ?> map)) {
            return null;
        }
        Long id = parseLong(map.get("id"));
        Integer rowNumber = parseInteger(map.get("rowNumber"));
        String fieldName = map.get("fieldName") == null ? null : String.valueOf(map.get("fieldName"));
        String errorMessage = map.get("errorMessage") == null ? null : String.valueOf(map.get("errorMessage"));
        String rawValue = map.get("rawValue") == null ? null : String.valueOf(map.get("rawValue"));
        LocalDateTime createdAt = parseDateTime(map.get("createdAt"));
        if (id == null || rowNumber == null || createdAt == null) {
            return null;
        }
        Long taskId = parseLong(map.get("taskId"));
        return new DataImportErrorItemResponse(id, taskId, rowNumber, fieldName, errorMessage, rawValue, createdAt);
    }

    private Map<String, Object> toImportErrorPayload(DataImportErrorItemResponse error) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("id", error.id());
        payload.put("taskId", error.taskId());
        payload.put("rowNumber", error.rowNumber());
        payload.put("fieldName", error.fieldName());
        payload.put("errorMessage", error.errorMessage());
        payload.put("rawValue", error.rawValue());
        payload.put("createdAt", error.createdAt() == null ? null : error.createdAt().toString());
        return payload;
    }

    private List<DataImportErrorItemResponse> filterLatestImportErrors(Long taskId, DataImportErrorFilterRequest request) {
        List<DataImportErrorItemResponse> items = listImportErrors(taskId);
        String normalizedFieldName = QueryFilterSupport.trimToNull(request.fieldName());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        return items.stream()
                .filter(item -> request.rowNumber() == null || request.rowNumber().equals(item.rowNumber()))
                .filter(item -> normalizedFieldName == null || normalizedFieldName.equalsIgnoreCase(item.fieldName()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.errorMessage(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.rawValue(), normalizedKeyword))
                .toList();
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private String buildAdvisorScopeTarget(AdvisorScopeBinding binding) {
        return binding.getAdvisorUsername() + "/" + binding.getGrade() + "/" + binding.getClassName() + "/student#" + binding.getStudentId();
    }
}
