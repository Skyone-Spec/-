package edu.ruc.platform.certificate.service;

import edu.ruc.platform.admin.repository.AdminOperationLogRepository;
import edu.ruc.platform.admin.domain.LatestSysOperationLog;
import edu.ruc.platform.admin.repository.LatestSysOperationLogRepository;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.auth.service.StudentDataScopeService;
import edu.ruc.platform.certificate.domain.LatestAffairRequest;
import edu.ruc.platform.certificate.domain.LatestCertApplication;
import edu.ruc.platform.certificate.domain.LatestWorkflowDefinition;
import edu.ruc.platform.certificate.domain.LatestWorkflowInstance;
import edu.ruc.platform.certificate.domain.LatestWorkflowNode;
import edu.ruc.platform.certificate.domain.LatestWorkflowTask;
import edu.ruc.platform.certificate.domain.LatestWorkflowTaskAction;
import edu.ruc.platform.certificate.dto.ApprovalActionRequest;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskFilterRequest;
import edu.ruc.platform.certificate.dto.ApprovalTaskResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskStatsResponse;
import edu.ruc.platform.certificate.dto.CertificatePreviewResponse;
import edu.ruc.platform.certificate.dto.CertificateRequestActionRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestCreateRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.certificate.repository.LatestAffairRequestRepository;
import edu.ruc.platform.certificate.repository.LatestCertApplicationRepository;
import edu.ruc.platform.certificate.repository.LatestWorkflowDefinitionRepository;
import edu.ruc.platform.certificate.repository.LatestWorkflowInstanceRepository;
import edu.ruc.platform.certificate.repository.LatestWorkflowNodeRepository;
import edu.ruc.platform.certificate.repository.LatestWorkflowTaskActionRepository;
import edu.ruc.platform.certificate.repository.LatestWorkflowTaskRepository;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.knowledge.domain.LatestCertTemplate;
import edu.ruc.platform.knowledge.domain.LatestFileObject;
import edu.ruc.platform.knowledge.repository.LatestCertTemplateRepository;
import edu.ruc.platform.knowledge.repository.LatestFileObjectRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Profile("kingbase")
@RequiredArgsConstructor
public class KingbaseCertificateService implements CertificateApplicationService {

    private static final String BUSINESS_TABLE = "campus.affair_request";

    private final LatestAffairRequestRepository latestAffairRequestRepository;
    private final LatestCertApplicationRepository latestCertApplicationRepository;
    private final LatestWorkflowDefinitionRepository latestWorkflowDefinitionRepository;
    private final LatestWorkflowInstanceRepository latestWorkflowInstanceRepository;
    private final LatestWorkflowNodeRepository latestWorkflowNodeRepository;
    private final LatestWorkflowTaskRepository latestWorkflowTaskRepository;
    private final LatestWorkflowTaskActionRepository latestWorkflowTaskActionRepository;
    private final LatestCertTemplateRepository latestCertTemplateRepository;
    private final LatestFileObjectRepository latestFileObjectRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CurrentUserService currentUserService;
    private final StudentDataScopeService studentDataScopeService;
    private final AdminOperationLogRepository adminOperationLogRepository;
    private final LatestSysOperationLogRepository latestSysOperationLogRepository;

    @Override
    public CertificateRequestResponse create(CertificateRequestCreateRequest request) {
        requireCertificateOwner(request.studentId());
        validateCertificateType(request.certificateType());
        LatestAffairRequest affairRequest = new LatestAffairRequest();
        affairRequest.setRequesterUserId(request.studentId());
        affairRequest.setRequestType("certificate");
        affairRequest.setTitle(normalizeCertificateType(request.certificateType()) + "申请");
        affairRequest.setPurpose(request.reason() == null || request.reason().isBlank() ? "平台申请" : request.reason().trim());
        affairRequest.setRemark(request.reason());
        affairRequest.setPayloadJson(buildPayloadJson(request));
        affairRequest.setStatus("submitted");
        affairRequest.setSubmittedAt(LocalDateTime.now());
        affairRequest.setClosedAt(null);
        affairRequest.setIsDeleted(0);
        affairRequest = latestAffairRequestRepository.save(affairRequest);

        LatestCertTemplate template = resolveTemplate(normalizeCertificateType(request.certificateType()));
        LatestCertApplication certApplication = new LatestCertApplication();
        certApplication.setRequestId(affairRequest.getId());
        certApplication.setTemplateId(template.getId());
        certApplication.setGeneratedPdfFileId(null);
        certApplication.setStudentSnapshotJson(buildStudentSnapshotJson(request.studentId()));
        certApplication = latestCertApplicationRepository.save(certApplication);

        initWorkflowIfPossible(affairRequest, certApplication);
        return toCertificateRequestResponse(certApplication, affairRequest);
    }

    @Override
    public List<CertificateRequestResponse> listByStudentId(Long studentId) {
        requireCertificateOwner(studentId);
        return latestAffairRequestRepository.findByRequesterUserIdAndIsDeletedOrderByCreatedAtDesc(studentId, 0).stream()
                .filter(item -> "certificate".equalsIgnoreCase(item.getRequestType()))
                .map(this::findCertApplicationByRequest)
                .flatMap(Optional::stream)
                .map(cert -> toCertificateRequestResponse(cert, requireAffairRequest(cert.getRequestId())))
                .toList();
    }

    @Override
    public List<ApprovalTaskResponse> listApprovalTasks() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return latestCertApplicationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(cert -> toApprovalTask(cert, requireAffairRequest(cert.getRequestId())))
                .filter(item -> canAccessApprovalTask(user, item.studentId()))
                .toList();
    }

    @Override
    public PageResponse<ApprovalTaskResponse> pageApprovalTasks(ApprovalTaskFilterRequest request, int page, int size) {
        List<ApprovalTaskResponse> filtered = filterApprovalTasks(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public ApprovalTaskStatsResponse approvalTaskStats(ApprovalTaskFilterRequest request) {
        List<ApprovalTaskResponse> filtered = filterApprovalTasks(request);
        return new ApprovalTaskStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> "PENDING".equals(item.status())).count(),
                (int) filtered.stream().filter(item -> "COUNSELOR_APPROVED".equals(item.status())).count(),
                (int) filtered.stream().filter(item -> "APPROVED".equals(item.status())).count(),
                (int) filtered.stream().filter(item -> "REJECTED".equals(item.status())).count(),
                (int) filtered.stream().filter(item -> "WITHDRAWN".equals(item.status())).count()
        );
    }

    @Override
    public ApprovalTaskResponse handleApproval(Long requestId, ApprovalActionRequest request) {
        LatestCertApplication certApplication = latestCertApplicationRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("审批单不存在"));
        LatestAffairRequest affairRequest = requireAffairRequest(certApplication.getRequestId());
        AuthenticatedUser operator = currentUserService.requireCurrentUser();
        validateOperatorPermission(affairRequest, operator, request.action());
        validateActionWindow(affairRequest, request.action());

        String fromStatus = toApprovalStatus(affairRequest.getStatus());
        String nextAffairStatus = resolveNextAffairStatus(affairRequest.getStatus(), request.action());
        affairRequest.setStatus(nextAffairStatus);
        if ("approved".equals(nextAffairStatus) || "rejected".equals(nextAffairStatus) || "canceled".equals(nextAffairStatus)) {
            affairRequest.setClosedAt(LocalDateTime.now());
        }
        latestAffairRequestRepository.save(affairRequest);

        latestWorkflowInstanceRepository.findByBusinessTableAndBusinessId(BUSINESS_TABLE, affairRequest.getId())
                .ifPresent(instance -> advanceWorkflow(instance, operator, request, nextAffairStatus));
        writeAdminOperationLog(certApplication, operator, request, toApprovalStatus(nextAffairStatus));
        return toApprovalTask(certApplication, affairRequest);
    }

    @Override
    public List<ApprovalHistoryResponse> listApprovalHistory(Long requestId) {
        LatestCertApplication certApplication = latestCertApplicationRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("审批单不存在或无权访问"));
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        LatestAffairRequest affairRequest = requireAffairRequest(certApplication.getRequestId());
        if (!canAccessApprovalTask(user, affairRequest.getRequesterUserId())) {
            throw new BusinessException("审批单不存在或无权访问");
        }
        return loadApprovalHistory(certApplication);
    }

    @Override
    public List<ApprovalHistoryResponse> listRequestHistory(Long requestId) {
        LatestCertApplication certApplication = requireRequestAccess(requestId);
        return loadApprovalHistory(certApplication);
    }

    @Override
    public CertificatePreviewResponse preview(Long requestId) {
        LatestCertApplication certApplication = requireRequestAccess(requestId);
        LatestAffairRequest affairRequest = requireAffairRequest(certApplication.getRequestId());
        String studentName = studentProfileRepository.findById(affairRequest.getRequesterUserId())
                .map(profile -> profile.getName())
                .orElse("待补充");
        String certificateType = resolveCertificateType(certApplication);
        String status = toApprovalStatus(affairRequest.getStatus());
        LocalDateTime withdrawalDeadline = resolveWithdrawalDeadline(affairRequest);
        boolean canWithdraw = withdrawalDeadline != null
                && LocalDateTime.now().isBefore(withdrawalDeadline)
                && ("PENDING".equalsIgnoreCase(status) || "COUNSELOR_APPROVED".equalsIgnoreCase(status));
        boolean canResubmit = "WITHDRAWN".equalsIgnoreCase(status);
        return new CertificatePreviewResponse(
                certApplication.getId(),
                affairRequest.getRequesterUserId(),
                studentName,
                certificateType,
                status,
                resolveCurrentApproverRole(status),
                resolveApprovalLevel(status),
                affairRequest.getPurpose(),
                buildPreviewUrl(certApplication),
                "PDF",
                buildTemplateFields(affairRequest, certificateType, studentName),
                buildGeneratedContent(certificateType, studentName),
                resolveNextStepHint(certificateType, status),
                affairRequest.getSubmittedAt() == null ? affairRequest.getCreatedAt() : affairRequest.getSubmittedAt(),
                withdrawalDeadline,
                canWithdraw,
                canResubmit
        );
    }

    @Override
    public CertificateRequestResponse handleStudentAction(Long requestId, CertificateRequestActionRequest request) {
        String action = request.action().trim().toLowerCase();
        if (!"withdraw".equals(action) && !"resubmit".equals(action)) {
            throw new BusinessException("学生端仅支持 withdraw 或 resubmit");
        }
        ApprovalTaskResponse updated = handleApproval(requestId, new ApprovalActionRequest(request.action(), request.comment()));
        LatestCertApplication certApplication = latestCertApplicationRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("审批单不存在"));
        return new CertificateRequestResponse(
                updated.requestId(),
                updated.studentId(),
                updated.certificateType(),
                updated.status(),
                buildPreviewUrl(certApplication)
        );
    }

    private void initWorkflowIfPossible(LatestAffairRequest affairRequest, LatestCertApplication certApplication) {
        latestWorkflowDefinitionRepository.findFirstByBusinessTypeAndIsActiveAndIsDeleted("affair_request", 1, 0)
                .ifPresent(definition -> {
                    LatestWorkflowInstance instance = new LatestWorkflowInstance();
                    instance.setWfId(definition.getId());
                    instance.setBusinessTable(BUSINESS_TABLE);
                    instance.setBusinessId(affairRequest.getId());
                    instance.setStatus("running");
                    instance.setStartedBy(affairRequest.getRequesterUserId());
                    instance.setStartedAt(LocalDateTime.now());
                    instance = latestWorkflowInstanceRepository.save(instance);

                    List<LatestWorkflowNode> nodes = latestWorkflowNodeRepository.findByWfIdAndIsDeletedOrderBySeqNoAsc(definition.getId(), 0);
                    if (!nodes.isEmpty()) {
                        LatestWorkflowNode firstNode = nodes.get(0);
                        LatestWorkflowTask task = new LatestWorkflowTask();
                        task.setWfInstanceId(instance.getId());
                        task.setWfNodeId(firstNode.getId());
                        task.setAssigneeUserId(firstNode.getApproverUserId());
                        task.setStatus("pending");
                        task.setDueAt(LocalDateTime.now().plusDays(1));
                        latestWorkflowTaskRepository.save(task);
                    }
                });
    }

    private void advanceWorkflow(LatestWorkflowInstance instance,
                                 AuthenticatedUser operator,
                                 ApprovalActionRequest request,
                                 String nextAffairStatus) {
        LatestWorkflowTask currentTask = latestWorkflowTaskRepository
                .findFirstByWfInstanceIdAndStatusOrderByCreatedAtAsc(instance.getId(), "pending")
                .orElse(null);
        if (currentTask == null) {
            return;
        }
        String normalizedAction = request.action().trim().toLowerCase();
        currentTask.setStatus("approve".equals(normalizedAction) || "resubmit".equals(normalizedAction) ? "approved"
                : "reject".equals(normalizedAction) ? "rejected"
                : "canceled");
        currentTask.setCompletedAt(LocalDateTime.now());
        latestWorkflowTaskRepository.save(currentTask);

        LatestWorkflowTaskAction action = new LatestWorkflowTaskAction();
        action.setWfTaskId(currentTask.getId());
        action.setActorUserId(operator.userId());
        action.setAction(normalizedAction.equals("withdraw") ? "cancel" : normalizedAction);
        action.setActionComment(request.comment());
        action.setActionAt(LocalDateTime.now());
        latestWorkflowTaskActionRepository.save(action);

        List<LatestWorkflowNode> nodes = latestWorkflowNodeRepository.findByWfIdAndIsDeletedOrderBySeqNoAsc(instance.getWfId(), 0);
        LatestWorkflowNode currentNode = nodes.stream().filter(node -> node.getId().equals(currentTask.getWfNodeId())).findFirst().orElse(null);
        if ("approve".equals(normalizedAction) && currentNode != null) {
            LatestWorkflowNode nextNode = nodes.stream()
                    .filter(node -> node.getSeqNo() == currentNode.getSeqNo() + 1)
                    .findFirst()
                    .orElse(null);
            if (nextNode != null) {
                LatestWorkflowTask nextTask = new LatestWorkflowTask();
                nextTask.setWfInstanceId(instance.getId());
                nextTask.setWfNodeId(nextNode.getId());
                nextTask.setAssigneeUserId(nextNode.getApproverUserId());
                nextTask.setStatus("pending");
                nextTask.setDueAt(LocalDateTime.now().plusDays(1));
                latestWorkflowTaskRepository.save(nextTask);
                instance.setStatus("running");
            } else {
                instance.setStatus("approved");
                instance.setFinishedAt(LocalDateTime.now());
            }
        } else if ("reject".equals(normalizedAction)) {
            instance.setStatus("rejected");
            instance.setFinishedAt(LocalDateTime.now());
        } else if ("withdraw".equals(normalizedAction)) {
            instance.setStatus("canceled");
            instance.setFinishedAt(LocalDateTime.now());
        } else if ("resubmit".equals(normalizedAction)) {
            instance.setStatus("running");
            if (currentNode != null) {
                LatestWorkflowTask reopened = new LatestWorkflowTask();
                reopened.setWfInstanceId(instance.getId());
                reopened.setWfNodeId(currentNode.getId());
                reopened.setAssigneeUserId(currentNode.getApproverUserId());
                reopened.setStatus("pending");
                reopened.setDueAt(LocalDateTime.now().plusDays(1));
                latestWorkflowTaskRepository.save(reopened);
            }
        }
        latestWorkflowInstanceRepository.save(instance);
    }

    private List<ApprovalHistoryResponse> loadApprovalHistory(LatestCertApplication certApplication) {
        LatestAffairRequest affairRequest = requireAffairRequest(certApplication.getRequestId());
        Optional<LatestWorkflowInstance> instance = latestWorkflowInstanceRepository.findByBusinessTableAndBusinessId(BUSINESS_TABLE, affairRequest.getId());
        if (instance.isEmpty()) {
            return List.of();
        }
        List<LatestWorkflowTask> tasks = latestWorkflowTaskRepository.findByWfInstanceIdOrderByCreatedAtAsc(instance.get().getId());
        if (tasks.isEmpty()) {
            return List.of();
        }
        List<Long> taskIds = tasks.stream().map(LatestWorkflowTask::getId).toList();
        return latestWorkflowTaskActionRepository.findByWfTaskIdInOrderByCreatedAtAsc(taskIds).stream()
                .map(item -> toApprovalHistory(item, certApplication.getId(), tasks))
                .toList();
    }

    private ApprovalHistoryResponse toApprovalHistory(LatestWorkflowTaskAction action, Long requestId, List<LatestWorkflowTask> tasks) {
        LatestWorkflowTask task = tasks.stream().filter(item -> item.getId().equals(action.getWfTaskId())).findFirst().orElse(null);
        int seq = task == null ? 1 : tasks.indexOf(task) + 1;
        String fromStatus = seq <= 1 ? "PENDING" : "COUNSELOR_APPROVED";
        String toStatus = switch (action.getAction()) {
            case "approve" -> seq <= 1 && tasks.size() > 1 ? "COUNSELOR_APPROVED" : "APPROVED";
            case "reject" -> "REJECTED";
            case "cancel" -> "WITHDRAWN";
            default -> "PENDING";
        };
        String operatorName = studentProfileRepository.findById(action.getActorUserId()).map(p -> p.getName()).orElse("系统用户");
        return new ApprovalHistoryResponse(
                action.getId(),
                requestId,
                action.getActorUserId(),
                operatorName,
                resolveOperatorRole(action.getActorUserId()),
                action.getAction(),
                fromStatus,
                toStatus,
                action.getActionComment(),
                action.getCreatedAt()
        );
    }

    private List<ApprovalTaskResponse> filterApprovalTasks(ApprovalTaskFilterRequest request) {
        validateApprovalTaskFilter(request);
        String normalizedStatus = QueryFilterSupport.normalizeUpper(request.status());
        String normalizedCertificateType = normalizeCertificateType(request.certificateType());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        return listApprovalTasks().stream()
                .filter(item -> request.studentId() == null || request.studentId().equals(item.studentId()))
                .filter(item -> normalizedStatus == null || normalizedStatus.equalsIgnoreCase(item.status()))
                .filter(item -> normalizedCertificateType == null || normalizedCertificateType.equals(item.certificateType()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.studentName(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.reason(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.certificateType(), normalizedKeyword))
                .toList();
    }

    private void validateApprovalTaskFilter(ApprovalTaskFilterRequest request) {
        if (request == null) {
            return;
        }
        QueryFilterSupport.requireEnumValue(edu.ruc.platform.common.enums.ApprovalStatus.class, request.status(), "审批状态不支持: ");
        if (request.certificateType() != null && !request.certificateType().isBlank()) {
            validateCertificateType(request.certificateType());
        }
    }

    private boolean canAccessApprovalTask(AuthenticatedUser user, Long studentId) {
        return studentDataScopeService.canAccessStudent(user, studentId);
    }

    private LatestCertApplication requireRequestAccess(Long requestId) {
        LatestCertApplication certApplication = latestCertApplicationRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("审批单不存在"));
        LatestAffairRequest affairRequest = requireAffairRequest(certApplication.getRequestId());
        currentUserService.requireSelfOrAdmin(affairRequest.getRequesterUserId(), RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return certApplication;
    }

    private LatestAffairRequest requireAffairRequest(Long requestId) {
        return latestAffairRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("关联申请不存在"));
    }

    private Optional<LatestCertApplication> findCertApplicationByRequest(LatestAffairRequest request) {
        return latestCertApplicationRepository.findByRequestId(request.getId());
    }

    private ApprovalTaskResponse toApprovalTask(LatestCertApplication certApplication, LatestAffairRequest affairRequest) {
        String studentName = studentProfileRepository.findById(affairRequest.getRequesterUserId())
                .map(profile -> profile.getName())
                .orElse("待补充");
        return new ApprovalTaskResponse(
                certApplication.getId(),
                affairRequest.getRequesterUserId(),
                studentName,
                resolveCertificateType(certApplication),
                toApprovalStatus(affairRequest.getStatus()),
                affairRequest.getPurpose(),
                affairRequest.getSubmittedAt() == null ? affairRequest.getCreatedAt() : affairRequest.getSubmittedAt()
        );
    }

    private CertificateRequestResponse toCertificateRequestResponse(LatestCertApplication certApplication, LatestAffairRequest affairRequest) {
        return new CertificateRequestResponse(
                certApplication.getId(),
                affairRequest.getRequesterUserId(),
                resolveCertificateType(certApplication),
                toApprovalStatus(affairRequest.getStatus()),
                buildPreviewUrl(certApplication)
        );
    }

    private String buildPayloadJson(CertificateRequestCreateRequest request) {
        return "{\"certificateType\":\"" + normalizeCertificateType(request.certificateType()) + "\"}";
    }

    private String buildStudentSnapshotJson(Long studentId) {
        return studentProfileRepository.findById(studentId)
                .map(profile -> "{\"studentNo\":\"" + profile.getStudentNo() + "\",\"fullName\":\"" + profile.getName() + "\"}")
                .orElse("{}");
    }

    private LatestCertTemplate resolveTemplate(String certificateType) {
        List<LatestCertTemplate> templates = latestCertTemplateRepository.findByIsDeletedAndIsActive(0, 1);
        return templates.stream()
                .filter(item -> item.getTemplateName() != null && item.getTemplateName().contains(certificateType.replace("证明", "")))
                .findFirst()
                .orElseGet(() -> templates.stream().findFirst().orElseThrow(() -> new BusinessException("未配置可用证明模板")));
    }

    private String resolveCertificateType(LatestCertApplication certApplication) {
        return latestCertTemplateRepository.findById(certApplication.getTemplateId())
                .map(LatestCertTemplate::getTemplateName)
                .map(name -> name.contains("在读") ? "在读证明" : name.contains("党员") ? "党员身份证明" : name.contains("困难") ? "困难认定证明" : name)
                .orElse("证明申请");
    }

    private String buildPreviewUrl(LatestCertApplication certApplication) {
        if (certApplication.getGeneratedPdfFileId() == null) {
            return "/exports/certificates/" + certApplication.getId() + ".pdf";
        }
        return latestFileObjectRepository.findById(certApplication.getGeneratedPdfFileId())
                .map(LatestFileObject::getStoragePath)
                .orElse("/exports/certificates/" + certApplication.getId() + ".pdf");
    }

    private String toApprovalStatus(String affairStatus) {
        return switch (affairStatus == null ? "" : affairStatus.toLowerCase()) {
            case "submitted" -> "PENDING";
            case "in_review" -> "COUNSELOR_APPROVED";
            case "approved", "closed" -> "APPROVED";
            case "rejected" -> "REJECTED";
            case "canceled" -> "WITHDRAWN";
            default -> "PENDING";
        };
    }

    private String resolveNextAffairStatus(String currentAffairStatus, String action) {
        String normalized = action.trim().toLowerCase();
        return switch (normalized) {
            case "approve" -> "submitted".equalsIgnoreCase(currentAffairStatus) ? "in_review" : "approved";
            case "reject" -> "rejected";
            case "withdraw" -> "canceled";
            case "resubmit" -> "submitted";
            default -> throw new BusinessException("审批动作仅支持 approve、reject、withdraw 或 resubmit");
        };
    }

    private int resolveApprovalLevel(String status) {
        return switch (status) {
            case "COUNSELOR_APPROVED" -> 2;
            case "APPROVED", "REJECTED", "WITHDRAWN" -> 3;
            default -> 1;
        };
    }

    private String resolveCurrentApproverRole(String status) {
        return switch (status) {
            case "PENDING" -> "COUNSELOR";
            case "COUNSELOR_APPROVED" -> "COLLEGE_ADMIN";
            default -> "NONE";
        };
    }

    private LocalDateTime resolveWithdrawalDeadline(LatestAffairRequest affairRequest) {
        LocalDateTime submittedAt = affairRequest.getSubmittedAt() == null ? affairRequest.getCreatedAt() : affairRequest.getSubmittedAt();
        return submittedAt == null ? null : submittedAt.plusDays(2);
    }

    private Map<String, String> buildTemplateFields(LatestAffairRequest affairRequest, String certificateType, String studentName) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("studentName", studentName);
        fields.put("studentId", String.valueOf(affairRequest.getRequesterUserId()));
        fields.put("certificateType", certificateType);
        fields.put("reason", affairRequest.getPurpose() == null ? "" : affairRequest.getPurpose());
        fields.put("submittedAt", String.valueOf(affairRequest.getSubmittedAt() == null ? affairRequest.getCreatedAt() : affairRequest.getSubmittedAt()));
        return fields;
    }

    private String buildGeneratedContent(String certificateType, String studentName) {
        return switch (certificateType) {
            case "在读证明" -> "兹证明 " + studentName + " 系本院在籍学生，当前学籍状态正常。";
            case "党员身份证明" -> "兹证明 " + studentName + " 的党员身份信息需结合组织档案核验。";
            case "困难认定证明" -> "兹证明 " + studentName + " 已提交困难认定相关申请，正式结果以学院审核为准。";
            default -> "兹证明 " + studentName + " 为本院相关学生。";
        };
    }

    private String resolveNextStepHint(String certificateType, String status) {
        if ("党员身份证明".equals(certificateType) && "COUNSELOR_APPROVED".equals(status)) {
            return "辅导员已通过，下一步将由学院终审。";
        }
        return switch (status) {
            case "PENDING" -> "等待辅导员初审。";
            case "COUNSELOR_APPROVED" -> "辅导员已通过，等待学院管理员终审。";
            case "APPROVED" -> "审批已完成，可下载正式证明文件。";
            case "WITHDRAWN" -> "申请已撤回，可按需重新提交。";
            case "REJECTED" -> "申请已驳回，建议补充材料后重新发起。";
            default -> "请关注后续审批进展。";
        };
    }

    private void validateOperatorPermission(LatestAffairRequest affairRequest, AuthenticatedUser operator, String action) {
        String normalizedAction = action.trim().toLowerCase();
        if ("withdraw".equals(normalizedAction) || "resubmit".equals(normalizedAction)) {
            if (!operator.userId().equals(affairRequest.getRequesterUserId())) {
                throw new BusinessException("当前账号无权执行该审批动作");
            }
            return;
        }
        if ("submitted".equalsIgnoreCase(affairRequest.getStatus())
                && !(RoleType.COUNSELOR.name().equals(operator.role())
                || RoleType.SUPER_ADMIN.name().equals(operator.role())
                || RoleType.COLLEGE_ADMIN.name().equals(operator.role()))) {
            throw new BusinessException("待初审申请仅辅导员或管理员可处理");
        }
        if ("in_review".equalsIgnoreCase(affairRequest.getStatus())
                && !(RoleType.COLLEGE_ADMIN.name().equals(operator.role())
                || RoleType.SUPER_ADMIN.name().equals(operator.role()))) {
            throw new BusinessException("初审通过后的申请仅学院管理员可终审");
        }
    }

    private void validateActionWindow(LatestAffairRequest affairRequest, String action) {
        String normalizedAction = action.trim().toLowerCase();
        if ("withdraw".equals(normalizedAction)) {
            LocalDateTime deadline = resolveWithdrawalDeadline(affairRequest);
            if (deadline == null || LocalDateTime.now().isAfter(deadline)) {
                throw new BusinessException("当前申请已超过撤回期限");
            }
            return;
        }
        if ("resubmit".equals(normalizedAction) && !"canceled".equalsIgnoreCase(affairRequest.getStatus())) {
            throw new BusinessException("仅已撤回的申请支持重新提交");
        }
    }

    private void validateCertificateType(String certificateType) {
        if (!List.of("在读证明", "党员身份证明", "困难认定证明").contains(normalizeCertificateType(certificateType))) {
            throw new BusinessException("证明类型仅支持 在读证明、党员身份证明、困难认定证明");
        }
    }

    private String normalizeCertificateType(String certificateType) {
        return certificateType == null ? null : certificateType.trim();
    }

    private void requireCertificateOwner(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        if (!user.userId().equals(studentId)) {
            throw new BusinessException("证明申请仅支持学生本人操作");
        }
    }

    private String resolveOperatorRole(Long userId) {
        if (userId == null) {
            return "ASSISTANT";
        }
        if (studentProfileRepository.findById(userId).isPresent()) {
            return "STUDENT";
        }
        return "COUNSELOR";
    }

    private void writeAdminOperationLog(LatestCertApplication certApplication,
                                        AuthenticatedUser operator,
                                        ApprovalActionRequest request,
                                        String nextStatus) {
        LatestSysOperationLog log = new LatestSysOperationLog();
        log.setModuleCode("approval");
        log.setBusinessType("cert_application");
        log.setBusinessId(certApplication.getId());
        log.setOperationType(request.action().trim().toLowerCase());
        log.setOperationDesc(request.comment() == null || request.comment().isBlank() ? "证明申请#" + certApplication.getId() : request.comment());
        log.setOperatorUserId(operator.userId());
        log.setTraceId("trace-approval-" + certApplication.getId() + "-" + System.currentTimeMillis());
        log.setRequestUri("/api/v1/admin/approvals/" + certApplication.getId() + "/actions");
        log.setRequestMethod("POST");
        log.setRequestIp(null);
        log.setUserAgent(null);
        log.setLogLevel("audit");
        log.setResultStatus(("APPROVED".equalsIgnoreCase(nextStatus) || "COUNSELOR_APPROVED".equalsIgnoreCase(nextStatus))
                ? "success"
                : "REJECTED".equalsIgnoreCase(nextStatus) ? "fail" : "partial");
        log.setErrorMessage("REJECTED".equalsIgnoreCase(nextStatus) ? request.comment() : null);
        log.setExtJson("{\"operatorName\":\"" + operator.name() + "\",\"operatorRole\":\"" + operator.role() + "\",\"target\":\"证明申请#" + certApplication.getId() + "\"}");
        log.setCreatedAt(LocalDateTime.now());
        latestSysOperationLogRepository.save(log);
    }
}
