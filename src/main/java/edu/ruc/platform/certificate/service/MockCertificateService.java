package edu.ruc.platform.certificate.service;

import edu.ruc.platform.certificate.dto.ApprovalActionRequest;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskFilterRequest;
import edu.ruc.platform.certificate.dto.ApprovalTaskResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskStatsResponse;
import edu.ruc.platform.certificate.dto.CertificatePreviewResponse;
import edu.ruc.platform.certificate.dto.CertificateRequestActionRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestCreateRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockCertificateService implements CertificateApplicationService {

    private final CurrentUserService currentUserService;
    private final AtomicLong idGenerator = new AtomicLong(2000);
    private final AtomicLong historyIdGenerator = new AtomicLong(3000);
    private final List<ApprovalTaskResponse> approvalTasks = new ArrayList<>(List.of(
            new ApprovalTaskResponse(1001L, 10001L, "张三", "在读证明", "PENDING", "奖学金申请材料需要", LocalDateTime.of(2026, 3, 20, 10, 30)),
            new ApprovalTaskResponse(1002L, 10002L, "李四", "党员身份证明", "COUNSELOR_APPROVED", "组织关系转接", LocalDateTime.of(2026, 3, 19, 15, 0))
    ));
    private final List<ApprovalHistoryResponse> approvalHistories = new ArrayList<>(List.of(
            new ApprovalHistoryResponse(3001L, 1002L, 20001L, "胡浩老师", "COUNSELOR", "approve", "PENDING", "COUNSELOR_APPROVED", "初审通过", LocalDateTime.of(2026, 3, 20, 9, 0))
    ));

    @Override
    public CertificateRequestResponse create(CertificateRequestCreateRequest request) {
        currentUserService.requireSelfOrAdmin(request.studentId(), RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        validateCertificateType(request.certificateType());
        long id = idGenerator.incrementAndGet();
        approvalTasks.add(0, new ApprovalTaskResponse(
                id,
                request.studentId(),
                resolveStudentName(request.studentId()),
                normalizeCertificateType(request.certificateType()),
                "PENDING",
                request.reason(),
                LocalDateTime.now()
        ));
        return new CertificateRequestResponse(id, request.studentId(), normalizeCertificateType(request.certificateType()), "PENDING", null);
    }

    @Override
    public List<CertificateRequestResponse> listByStudentId(Long studentId) {
        currentUserService.requireSelfOrAdmin(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return approvalTasks.stream()
                .filter(task -> task.studentId().equals(studentId))
                .map(task -> new CertificateRequestResponse(
                        task.requestId(),
                        task.studentId(),
                        task.certificateType(),
                        task.status(),
                        "/exports/certificates/" + task.requestId() + ".pdf"
                ))
                .toList();
    }

    @Override
    public List<ApprovalTaskResponse> listApprovalTasks() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return approvalTasks.stream()
                .filter(item -> canAccessApprovalTask(user, item))
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
        for (int i = 0; i < approvalTasks.size(); i++) {
            ApprovalTaskResponse task = approvalTasks.get(i);
            if (task.requestId().equals(requestId)) {
                validateOperatorPermission(task, request.action());
                validateActionWindow(task, request.action());
                String fromStatus = task.status();
                String nextStatus = resolveNextStatus(task.status(), request.action());
                ApprovalTaskResponse updated = new ApprovalTaskResponse(
                        task.requestId(),
                        task.studentId(),
                        task.studentName(),
                        task.certificateType(),
                        nextStatus,
                        request.comment() == null || request.comment().isBlank() ? task.reason() : request.comment(),
                        task.submittedAt()
                );
                approvalTasks.set(i, updated);
                AuthenticatedUser operator = currentUserService.requireCurrentUser();
                approvalHistories.add(new ApprovalHistoryResponse(
                        historyIdGenerator.incrementAndGet(),
                        requestId,
                        operator.userId(),
                        operator.name(),
                        operator.role(),
                        request.action().toLowerCase(),
                        fromStatus,
                        nextStatus,
                        request.comment(),
                        LocalDateTime.now()
                ));
                return updated;
            }
        }
        throw new BusinessException("审批单不存在: " + requestId);
    }

    @Override
    public List<ApprovalHistoryResponse> listApprovalHistory(Long requestId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        ApprovalTaskResponse task = findTask(requestId);
        if (!canAccessApprovalTask(user, task)) {
            throw new BusinessException("审批单不存在或无权访问");
        }
        return approvalHistories.stream()
                .filter(item -> item.requestId().equals(requestId))
                .toList();
    }

    @Override
    public List<ApprovalHistoryResponse> listRequestHistory(Long requestId) {
        ApprovalTaskResponse task = findTask(requestId);
        currentUserService.requireSelfOrAdmin(task.studentId(), RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return approvalHistories.stream()
                .filter(item -> item.requestId().equals(requestId))
                .toList();
    }

    @Override
    public CertificatePreviewResponse preview(Long requestId) {
        ApprovalTaskResponse task = findTask(requestId);
        currentUserService.requireSelfOrAdmin(task.studentId(), RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return toPreview(task);
    }

    @Override
    public CertificateRequestResponse handleStudentAction(Long requestId, CertificateRequestActionRequest request) {
        ApprovalTaskResponse task = findTask(requestId);
        currentUserService.requireSelfOrAdmin(task.studentId(), RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
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
                buildPdfPath(updated.requestId())
        );
    }

    private String resolveNextStatus(String currentStatus, String action) {
        return switch (action.toLowerCase()) {
            case "approve" -> "PENDING".equals(currentStatus) ? "COUNSELOR_APPROVED" : "APPROVED";
            case "reject" -> "REJECTED";
            case "withdraw" -> "WITHDRAWN";
            case "resubmit" -> "PENDING";
            default -> throw new BusinessException("审批动作不支持: " + action);
        };
    }

    private void validateActionWindow(ApprovalTaskResponse task, String action) {
        String normalizedAction = action.toLowerCase();
        if ("withdraw".equals(normalizedAction)) {
            if (!"PENDING".equalsIgnoreCase(task.status())) {
                throw new BusinessException("仅待初审状态的申请支持撤回");
            }
            if (task.submittedAt().plusDays(2).isBefore(LocalDateTime.now())) {
                throw new BusinessException("当前申请已超过撤回期限");
            }
            return;
        }
        if ("resubmit".equals(normalizedAction)) {
            if (!"WITHDRAWN".equalsIgnoreCase(task.status()) && !"REJECTED".equalsIgnoreCase(task.status())) {
                throw new BusinessException("仅已撤回或已驳回的申请支持重新提交");
            }
            return;
        }
        if (("approve".equals(normalizedAction) || "reject".equals(normalizedAction))
                && ("WITHDRAWN".equalsIgnoreCase(task.status()) || "APPROVED".equalsIgnoreCase(task.status()))) {
            throw new BusinessException("当前审批状态不允许继续处理");
        }
    }

    private void validateOperatorPermission(ApprovalTaskResponse task, String action) {
        AuthenticatedUser operator = currentUserService.requireCurrentUser();
        String normalizedAction = action.toLowerCase();
        if ("withdraw".equals(normalizedAction) || "resubmit".equals(normalizedAction)) {
            if (!operator.userId().equals(task.studentId())
                    && !RoleType.SUPER_ADMIN.name().equals(operator.role())
                    && !RoleType.COLLEGE_ADMIN.name().equals(operator.role())
                    && !RoleType.COUNSELOR.name().equals(operator.role())) {
                throw new BusinessException("当前账号无权执行该审批动作");
            }
            return;
        }
        if ("approve".equals(normalizedAction) || "reject".equals(normalizedAction)) {
            if ("PENDING".equalsIgnoreCase(task.status())
                    && !(RoleType.COUNSELOR.name().equals(operator.role())
                    || RoleType.SUPER_ADMIN.name().equals(operator.role())
                    || RoleType.COLLEGE_ADMIN.name().equals(operator.role()))) {
                throw new BusinessException("待初审申请仅辅导员或管理员可处理");
            }
            if ("COUNSELOR_APPROVED".equalsIgnoreCase(task.status())
                    && !(RoleType.COLLEGE_ADMIN.name().equals(operator.role())
                    || RoleType.SUPER_ADMIN.name().equals(operator.role()))) {
                throw new BusinessException("辅导员初审通过后的申请仅学院管理员可终审");
            }
        }
    }

    private List<ApprovalTaskResponse> filterApprovalTasks(ApprovalTaskFilterRequest request) {
        validateApprovalTaskFilter(request);
        String normalizedStatus = QueryFilterSupport.normalizeUpper(request.status());
        String normalizedCertificateType = normalizeCertificateType(request.certificateType());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        return approvalTasks.stream()
                .filter(item -> canAccessApprovalTask(currentUserService.requireCurrentUser(), item))
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

    private ApprovalTaskResponse findTask(Long requestId) {
        return approvalTasks.stream()
                .filter(item -> item.requestId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("审批单不存在"));
    }

    private CertificatePreviewResponse toPreview(ApprovalTaskResponse task) {
        boolean canWithdraw = "PENDING".equalsIgnoreCase(task.status()) || "COUNSELOR_APPROVED".equalsIgnoreCase(task.status());
        boolean canResubmit = "WITHDRAWN".equalsIgnoreCase(task.status());
        String currentApproverRole = switch (task.status()) {
            case "PENDING" -> "COUNSELOR";
            case "COUNSELOR_APPROVED" -> "COLLEGE_ADMIN";
            default -> "NONE";
        };
        int approvalLevel = switch (task.status()) {
            case "COUNSELOR_APPROVED" -> 2;
            case "APPROVED", "REJECTED", "WITHDRAWN" -> 3;
            default -> 1;
        };
        return new CertificatePreviewResponse(
                task.requestId(),
                task.studentId(),
                task.studentName(),
                task.certificateType(),
                task.status(),
                currentApproverRole,
                approvalLevel,
                task.reason(),
                buildPdfPath(task.requestId()),
                "PDF",
                buildTemplateFields(task),
                buildGeneratedContent(task),
                resolveNextStepHint(task.certificateType(), task.status()),
                task.submittedAt(),
                task.submittedAt().plusDays(2),
                canWithdraw,
                canResubmit
        );
    }

    private String buildPdfPath(Long requestId) {
        return "/exports/certificates/" + requestId + ".pdf";
    }

    private String resolveNextStepHint(String status) {
        return resolveNextStepHint("通用证明", status);
    }

    private Map<String, String> buildTemplateFields(ApprovalTaskResponse task) {
        Map<String, String> fields = new java.util.LinkedHashMap<>();
        fields.put("studentName", task.studentName());
        fields.put("studentId", String.valueOf(task.studentId()));
        fields.put("certificateType", task.certificateType());
        fields.put("reason", task.reason() == null ? "" : task.reason());
        fields.put("submittedAt", String.valueOf(task.submittedAt()));
        if ("在读证明".equals(task.certificateType())) {
            fields.put("studentStatus", "在籍");
            fields.put("usageHint", "用于奖学金、课程证明等场景");
        } else if ("党员身份证明".equals(task.certificateType())) {
            fields.put("partyStatus", "党员身份待学院核验");
            fields.put("usageHint", "用于组织关系转接、政治审查等场景");
        } else if ("困难认定证明".equals(task.certificateType())) {
            fields.put("financialAidStatus", "需结合困难认定材料复核");
            fields.put("usageHint", "用于资助申请、困难认定等场景");
        }
        return fields;
    }

    private String buildGeneratedContent(ApprovalTaskResponse task) {
        return switch (task.certificateType()) {
            case "在读证明" -> "兹证明 " + task.studentName() + " 系本院在籍学生，当前学籍状态正常。本证明适用于课程修读、奖学金申请等场景，正式版本以审批通过后的 PDF 为准。";
            case "党员身份证明" -> "兹证明 " + task.studentName() + " 的党员身份信息需结合组织发展档案核验。本预览仅用于流程确认，正式证明以学院党组织审核结果为准。";
            case "困难认定证明" -> "兹证明 " + task.studentName() + " 已提交困难认定证明申请，需结合佐证材料与学院审核结果生成正式文件，预览内容不直接替代正式认定结论。";
            default -> "兹证明 " + task.studentName() + " 当前在学院学生综合服务平台中已提交" + task.certificateType() + "申请，预览内容以审核通过后的正式 PDF 为准。";
        };
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

    private void validateCertificateType(String certificateType) {
        if (!List.of("在读证明", "党员身份证明", "困难认定证明").contains(normalizeCertificateType(certificateType))) {
            throw new BusinessException("证明类型仅支持 在读证明、党员身份证明、困难认定证明");
        }
    }

    private String normalizeCertificateType(String certificateType) {
        return certificateType == null ? null : certificateType.trim();
    }

    private String resolveStudentName(Long studentId) {
        return switch (studentId.intValue()) {
            case 10001 -> "张三";
            case 10002 -> "李四";
            default -> "新申请学生";
        };
    }

    private boolean canAccessApprovalTask(AuthenticatedUser user, ApprovalTaskResponse task) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return true;
        }
        if (!"CLASS_ADVISOR".equals(user.role())) {
            return false;
        }
        return user.grade() != null && (
                ("advisor01".equals(user.username()) && task.studentId().equals(10001L))
                        || ("advisor02".equals(user.username()) && task.studentId().equals(10002L))
        );
    }
}
