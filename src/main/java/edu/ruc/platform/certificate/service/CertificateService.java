package edu.ruc.platform.certificate.service;

import edu.ruc.platform.certificate.dto.ApprovalActionRequest;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskFilterRequest;
import edu.ruc.platform.certificate.dto.ApprovalTaskResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskStatsResponse;
import edu.ruc.platform.certificate.dto.CertificatePreviewResponse;
import edu.ruc.platform.certificate.dto.CertificateRequestActionRequest;
import edu.ruc.platform.certificate.domain.ApprovalActionLog;
import edu.ruc.platform.certificate.domain.CertificateRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestCreateRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.certificate.repository.ApprovalActionLogRepository;
import edu.ruc.platform.certificate.repository.CertificateRequestRepository;
import edu.ruc.platform.admin.domain.AdminOperationLog;
import edu.ruc.platform.admin.repository.AdminOperationLogRepository;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.auth.service.StudentDataScopeService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.student.domain.AdvisorScopeBinding;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.AdvisorScopeBindingRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class CertificateService implements CertificateApplicationService {

    private final CertificateRequestRepository certificateRequestRepository;
    private final ApprovalActionLogRepository approvalActionLogRepository;
    private final AdminOperationLogRepository adminOperationLogRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AdvisorScopeBindingRepository advisorScopeBindingRepository;
    private final CurrentUserService currentUserService;
    private final StudentDataScopeService studentDataScopeService;

    @Override
    public CertificateRequestResponse create(CertificateRequestCreateRequest request) {
        currentUserService.requireSelfOrAdmin(request.studentId(), RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        validateCertificateType(request.certificateType());
        CertificateRequest entity = new CertificateRequest();
        entity.setStudentId(request.studentId());
        entity.setCertificateType(normalizeCertificateType(request.certificateType()));
        entity.setReason(request.reason());
        entity.setStatus("PENDING");
        entity.setApprovalLevel(1);
        entity.setCurrentApproverRole("COUNSELOR");
        entity.setWithdrawalDeadline(LocalDateTime.now().plusDays(2));
        entity = certificateRequestRepository.save(entity);
        return new CertificateRequestResponse(
                entity.getId(),
                entity.getStudentId(),
                entity.getCertificateType(),
                entity.getStatus(),
                entity.getGeneratedPdfPath()
        );
    }

    @Override
    public List<CertificateRequestResponse> listByStudentId(Long studentId) {
        currentUserService.requireSelfOrAdmin(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return certificateRequestRepository.findByStudentId(studentId)
                .stream()
                .map(entity -> new CertificateRequestResponse(
                        entity.getId(),
                        entity.getStudentId(),
                        entity.getCertificateType(),
                        entity.getStatus(),
                        entity.getGeneratedPdfPath()
                ))
                .toList();
    }

    @Override
    public List<ApprovalTaskResponse> listApprovalTasks() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return certificateRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(entity -> canAccessApprovalTask(user, entity.getStudentId()))
                .map(entity -> new ApprovalTaskResponse(
                        entity.getId(),
                        entity.getStudentId(),
                        studentProfileRepository.findById(entity.getStudentId())
                                .map(profile -> profile.getName())
                                .orElse("待补充"),
                        entity.getCertificateType(),
                        entity.getStatus(),
                        entity.getReason(),
                        entity.getCreatedAt() == null ? LocalDateTime.now() : entity.getCreatedAt()
                ))
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
    public List<ApprovalHistoryResponse> listApprovalHistory(Long requestId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        CertificateRequest entity = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("审批单不存在或无权访问"));
        if (!canAccessApprovalTask(user, entity.getStudentId())) {
            throw new BusinessException("审批单不存在或无权访问");
        }
        return approvalActionLogRepository.findByRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(log -> new ApprovalHistoryResponse(
                        log.getId(),
                        log.getRequestId(),
                        log.getOperatorId(),
                        log.getOperatorName(),
                        log.getOperatorRole(),
                        log.getAction(),
                        log.getFromStatus(),
                        log.getToStatus(),
                        log.getComment(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public List<ApprovalHistoryResponse> listRequestHistory(Long requestId) {
        CertificateRequest entity = requireRequestAccess(requestId);
        return approvalActionLogRepository.findByRequestIdOrderByCreatedAtAsc(entity.getId()).stream()
                .map(log -> new ApprovalHistoryResponse(
                        log.getId(),
                        log.getRequestId(),
                        log.getOperatorId(),
                        log.getOperatorName(),
                        log.getOperatorRole(),
                        log.getAction(),
                        log.getFromStatus(),
                        log.getToStatus(),
                        log.getComment(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public CertificatePreviewResponse preview(Long requestId) {
        CertificateRequest entity = requireRequestAccess(requestId);
        StudentProfile student = studentProfileRepository.findById(entity.getStudentId()).orElse(null);
        boolean canWithdraw = entity.getWithdrawalDeadline() != null
                && LocalDateTime.now().isBefore(entity.getWithdrawalDeadline())
                && ("PENDING".equalsIgnoreCase(entity.getStatus()) || "COUNSELOR_APPROVED".equalsIgnoreCase(entity.getStatus()));
        boolean canResubmit = "WITHDRAWN".equalsIgnoreCase(entity.getStatus());
        String studentName = student == null ? "待补充" : student.getName();
        return new CertificatePreviewResponse(
                entity.getId(),
                entity.getStudentId(),
                studentName,
                entity.getCertificateType(),
                entity.getStatus(),
                entity.getCurrentApproverRole(),
                entity.getApprovalLevel(),
                entity.getReason(),
                buildPdfPath(entity),
                "PDF",
                buildTemplateFields(entity, studentName),
                buildGeneratedContent(entity, studentName),
                resolveNextStepHint(entity.getCertificateType(), entity.getStatus()),
                entity.getCreatedAt(),
                entity.getWithdrawalDeadline(),
                canWithdraw,
                canResubmit
        );
    }

    @Override
    public CertificateRequestResponse handleStudentAction(Long requestId, CertificateRequestActionRequest request) {
        CertificateRequest entity = requireRequestAccess(requestId);
        String action = request.action().trim().toLowerCase();
        if (!"withdraw".equals(action) && !"resubmit".equals(action)) {
            throw new BusinessException("学生端仅支持 withdraw 或 resubmit");
        }
        ApprovalTaskResponse updated = handleApproval(requestId, new ApprovalActionRequest(request.action(), request.comment()));
        return new CertificateRequestResponse(
                updated.requestId(),
                updated.studentId(),
                updated.certificateType(),
                updated.status(),
                buildPdfPath(entity)
        );
    }

    private String resolveNextStatus(String currentStatus, String action) {
        String normalizedAction = action.trim().toLowerCase();
        return switch (normalizedAction) {
            case "approve" -> "PENDING".equalsIgnoreCase(currentStatus) ? "COUNSELOR_APPROVED" : "APPROVED";
            case "reject" -> "REJECTED";
            case "withdraw" -> "WITHDRAWN";
            case "resubmit" -> "PENDING";
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

    private Map<String, String> buildTemplateFields(CertificateRequest entity, String studentName) {
        Map<String, String> fields = new java.util.LinkedHashMap<>();
        fields.put("studentName", studentName);
        fields.put("studentId", String.valueOf(entity.getStudentId()));
        fields.put("certificateType", entity.getCertificateType());
        fields.put("reason", entity.getReason() == null ? "" : entity.getReason());
        fields.put("submittedAt", String.valueOf(entity.getCreatedAt()));
        if ("在读证明".equals(entity.getCertificateType())) {
            fields.put("studentStatus", "在籍");
            fields.put("usageHint", "用于奖学金、课程证明等场景");
        } else if ("党员身份证明".equals(entity.getCertificateType())) {
            fields.put("partyStatus", "党员身份待学院核验");
            fields.put("usageHint", "用于组织关系转接、政治审查等场景");
        } else if ("困难认定证明".equals(entity.getCertificateType())) {
            fields.put("financialAidStatus", "需结合困难认定材料复核");
            fields.put("usageHint", "用于资助申请、困难认定等场景");
        }
        return fields;
    }

    private String resolveCurrentApproverRole(String status) {
        return switch (status) {
            case "PENDING" -> "COUNSELOR";
            case "COUNSELOR_APPROVED" -> "COLLEGE_ADMIN";
            default -> "NONE";
        };
    }

    private void writeApprovalLog(CertificateRequest entity,
                                  AuthenticatedUser operator,
                                  ApprovalActionRequest request,
                                  String fromStatus,
                                  String nextStatus) {
        ApprovalActionLog log = new ApprovalActionLog();
        log.setRequestId(entity.getId());
        log.setOperatorId(operator.userId());
        log.setOperatorName(operator.name());
        log.setOperatorRole(operator.role());
        log.setAction(request.action().trim().toLowerCase());
        log.setFromStatus(fromStatus);
        log.setToStatus(nextStatus);
        log.setComment(request.comment());
        approvalActionLogRepository.save(log);
    }

    private void writeAdminOperationLog(CertificateRequest entity,
                                        AuthenticatedUser operator,
                                        ApprovalActionRequest request,
                                        String nextStatus) {
        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(operator.userId());
        log.setOperatorName(operator.name());
        log.setOperatorRole(operator.role());
        log.setModule("APPROVAL");
        log.setAction(request.action().trim().toUpperCase());
        log.setTarget("证明申请#" + entity.getId());
        log.setResult(nextStatus);
        log.setDetail(request.comment());
        adminOperationLogRepository.save(log);
    }

    @Override
    public ApprovalTaskResponse handleApproval(Long requestId, ApprovalActionRequest request) {
        CertificateRequest entity = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("审批单不存在"));
        AuthenticatedUser operator = currentUserService.requireCurrentUser();
        validateOperatorPermission(entity, operator, request.action());
        validateActionWindow(entity, request.action());
        String fromStatus = entity.getStatus();
        String nextStatus = resolveNextStatus(entity.getStatus(), request.action());
        entity.setStatus(nextStatus);
        entity.setApprovalLevel(resolveApprovalLevel(nextStatus));
        entity.setCurrentApproverRole(resolveCurrentApproverRole(nextStatus));
        if ("resubmit".equalsIgnoreCase(request.action())) {
            entity.setWithdrawalDeadline(LocalDateTime.now().plusDays(2));
        }
        if (request.comment() != null && !request.comment().isBlank()) {
            entity.setReason(request.comment());
        }
        entity = certificateRequestRepository.save(entity);
        writeApprovalLog(entity, operator, request, fromStatus, nextStatus);
        writeAdminOperationLog(entity, operator, request, nextStatus);
        return new ApprovalTaskResponse(
                entity.getId(),
                entity.getStudentId(),
                studentProfileRepository.findById(entity.getStudentId())
                        .map(profile -> profile.getName())
                        .orElse("待补充"),
                entity.getCertificateType(),
                entity.getStatus(),
                entity.getReason(),
                entity.getCreatedAt() == null ? LocalDateTime.now() : entity.getCreatedAt()
        );
    }

    private void validateOperatorPermission(CertificateRequest entity, AuthenticatedUser operator, String action) {
        String normalizedAction = action.trim().toLowerCase();
        if ("withdraw".equals(normalizedAction) || "resubmit".equals(normalizedAction)) {
            if (!operator.userId().equals(entity.getStudentId())
                    && !RoleType.SUPER_ADMIN.name().equals(operator.role())
                    && !RoleType.COLLEGE_ADMIN.name().equals(operator.role())
                    && !RoleType.COUNSELOR.name().equals(operator.role())) {
                throw new BusinessException("当前账号无权执行该审批动作");
            }
            return;
        }
        if (!"approve".equals(normalizedAction) && !"reject".equals(normalizedAction)) {
            return;
        }
        if ("PENDING".equalsIgnoreCase(entity.getStatus())
                && !(RoleType.COUNSELOR.name().equals(operator.role())
                || RoleType.SUPER_ADMIN.name().equals(operator.role())
                || RoleType.COLLEGE_ADMIN.name().equals(operator.role()))) {
            throw new BusinessException("待初审申请仅辅导员或管理员可处理");
        }
        if ("COUNSELOR_APPROVED".equalsIgnoreCase(entity.getStatus())
                && !(RoleType.COLLEGE_ADMIN.name().equals(operator.role())
                || RoleType.SUPER_ADMIN.name().equals(operator.role()))) {
            throw new BusinessException("辅导员初审通过后的申请仅学院管理员可终审");
        }
    }

    private void validateActionWindow(CertificateRequest entity, String action) {
        String normalizedAction = action.trim().toLowerCase();
        if ("withdraw".equals(normalizedAction)) {
            if (entity.getWithdrawalDeadline() == null || LocalDateTime.now().isAfter(entity.getWithdrawalDeadline())) {
                throw new BusinessException("当前申请已超过撤回期限");
            }
            return;
        }
        if ("resubmit".equals(normalizedAction) && !"WITHDRAWN".equalsIgnoreCase(entity.getStatus())) {
            throw new BusinessException("仅已撤回的申请支持重新提交");
        }
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

    private CertificateRequest requireRequestAccess(Long requestId) {
        CertificateRequest entity = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("审批单不存在"));
        currentUserService.requireSelfOrAdmin(entity.getStudentId(), RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return entity;
    }

    private String buildPdfPath(CertificateRequest entity) {
        return entity.getGeneratedPdfPath() == null || entity.getGeneratedPdfPath().isBlank()
                ? "/exports/certificates/" + entity.getId() + ".pdf"
                : entity.getGeneratedPdfPath();
    }

    private String resolveNextStepHint(String certificateType, String status) {
        if ("党员身份证明".equals(certificateType) && "COUNSELOR_APPROVED".equals(status)) {
            return "辅导员已通过，下一步将由学院党务口径终审党员身份信息。";
        }
        if ("党员身份证明".equals(certificateType) && "APPROVED".equals(status)) {
            return "党员身份证明已完成审核，可下载正式文件并用于组织关系转接等场景。";
        }
        if ("困难认定证明".equals(certificateType) && "PENDING".equals(status)) {
            return "等待辅导员初审，建议同步补齐资助申请或困难认定佐证材料。";
        }
        if ("在读证明".equals(certificateType) && "APPROVED".equals(status)) {
            return "审批已完成，可下载在读证明正式文件。";
        }
        return switch (status) {
            case "PENDING" -> "等待辅导员初审，建议保持联系方式畅通。";
            case "COUNSELOR_APPROVED" -> "辅导员已通过，等待学院管理员终审。";
            case "APPROVED" -> "审批已完成，可下载正式证明文件。";
            case "WITHDRAWN" -> "申请已撤回，如材料已修正可重新提交。";
            case "REJECTED" -> "申请已驳回，建议根据意见补充材料后重新发起。";
            default -> "请关注后续审批进展。";
        };
    }

    private String buildGeneratedContent(CertificateRequest entity, String studentName) {
        return switch (entity.getCertificateType()) {
            case "在读证明" -> "兹证明 " + studentName + " 系本院在籍学生，当前学籍状态正常。本证明适用于课程修读、奖学金申请等场景，正式版本以审批通过后的 PDF 为准。";
            case "党员身份证明" -> "兹证明 " + studentName + " 的党员身份信息需结合组织发展档案核验。本预览仅用于流程确认，正式证明以学院党组织审核结果为准。";
            case "困难认定证明" -> "兹证明 " + studentName + " 已提交困难认定证明申请，需结合佐证材料与学院审核结果生成正式文件，预览内容不直接替代正式认定结论。";
            default -> "兹证明 " + studentName + " 为本院相关学生。本预览由学院学生综合服务平台生成，正式版本以审批通过后的导出文件为准。";
        };
    }

    private void validateCertificateType(String certificateType) {
        if (!List.of("在读证明", "党员身份证明", "困难认定证明").contains(normalizeCertificateType(certificateType))) {
            throw new BusinessException("证明类型仅支持 在读证明、党员身份证明、困难认定证明");
        }
    }

    private String normalizeCertificateType(String certificateType) {
        return certificateType == null ? null : certificateType.trim();
    }
}
