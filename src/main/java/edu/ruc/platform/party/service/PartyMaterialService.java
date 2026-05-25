package edu.ruc.platform.party.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.auth.service.StudentDataScopeService;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.domain.PartyActionLog;
import edu.ruc.platform.party.domain.PartyMaterialSubmission;
import edu.ruc.platform.party.dto.*;
import edu.ruc.platform.party.repository.PartyActionLogRepository;
import edu.ruc.platform.party.repository.PartyMaterialSubmissionRepository;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Profile("!mock & !kingbase")
@RequiredArgsConstructor
public class PartyMaterialService implements PartyMaterialApplicationService {

    private final PartyMaterialSubmissionRepository submissionRepository;
    private final PartyActionLogRepository actionLogRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CurrentUserService currentUserService;
    private final StudentDataScopeService studentDataScopeService;

    @Override
    public PartyMaterialSubmissionResponse submitMaterial(PartyMaterialSubmitRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        validateStudentAccess(user, request.studentId());
        
        PartyMaterialSubmission entity = new PartyMaterialSubmission();
        entity.setStudentId(request.studentId());
        entity.setFlowType(request.flowType());
        entity.setStageName(request.stageName());
        entity.setMaterialType(request.materialType());
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setAttachmentPath(request.attachmentPath());
        entity.setStatus("PENDING");
        entity.setWithdrawalDeadline(LocalDateTime.now().plusDays(2));
        entity = submissionRepository.save(entity);
        
        writeActionLog(entity, user, "SUBMIT", "MATERIAL", entity.getId(), "提交材料");
        return toResponse(entity);
    }

    @Override
    public PartyMaterialSubmissionResponse reviewMaterial(Long submissionId, PartyMaterialReviewRequest request) {
        PartyMaterialSubmission entity = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException("材料提交记录不存在"));
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        validateReviewerPermission(user);
        
        String action = request.action().trim().toLowerCase();
        String fromStatus = entity.getStatus();
        String nextStatus;
        
        switch (action) {
            case "approve" -> {
                nextStatus = "APPROVED";
                entity.setReviewedAt(LocalDateTime.now());
            }
            case "reject" -> {
                nextStatus = "REJECTED";
                entity.setReviewedAt(LocalDateTime.now());
            }
            case "return" -> {
                nextStatus = "RETURNED";
                entity.setReviewedAt(LocalDateTime.now());
            }
            default -> throw new BusinessException("审批动作仅支持 approve、reject 或 return");
        }
        
        entity.setStatus(nextStatus);
        entity.setReviewerId(user.userId());
        entity.setReviewerName(user.name());
        entity.setReviewComment(request.reviewComment());
        entity = submissionRepository.save(entity);
        
        writeActionLog(entity, user, action.toUpperCase(), "MATERIAL", entity.getId(), request.reviewComment());
        return toResponse(entity);
    }

    @Override
    public PartyMaterialSubmissionResponse withdrawMaterial(Long submissionId) {
        PartyMaterialSubmission entity = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException("材料提交记录不存在"));
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        
        if (!user.userId().equals(entity.getStudentId())) {
            throw new BusinessException("仅提交人本人可撤回");
        }
        if (!"PENDING".equals(entity.getStatus()) && !"RETURNED".equals(entity.getStatus())) {
            throw new BusinessException("当前状态不允许撤回");
        }
        if (entity.getWithdrawalDeadline() != null && LocalDateTime.now().isAfter(entity.getWithdrawalDeadline())) {
            throw new BusinessException("已超过撤回期限（2天）");
        }
        
        entity.setStatus("WITHDRAWN");
        entity = submissionRepository.save(entity);
        writeActionLog(entity, user, "WITHDRAW", "MATERIAL", entity.getId(), "撤回材料提交");
        return toResponse(entity);
    }

    @Override
    public PartyMaterialSubmissionResponse resubmitMaterial(Long submissionId, PartyMaterialSubmitRequest request) {
        PartyMaterialSubmission entity = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException("材料提交记录不存在"));
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        
        if (!user.userId().equals(entity.getStudentId())) {
            throw new BusinessException("仅提交人本人可重新提交");
        }
        if (!"RETURNED".equals(entity.getStatus()) && !"WITHDRAWN".equals(entity.getStatus())) {
            throw new BusinessException("当前状态不允许重新提交");
        }
        
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setAttachmentPath(request.attachmentPath());
        entity.setStatus("PENDING");
        entity.setWithdrawalDeadline(LocalDateTime.now().plusDays(2));
        entity.setReviewComment(null);
        entity.setReviewerId(null);
        entity.setReviewerName(null);
        entity.setReviewedAt(null);
        entity = submissionRepository.save(entity);
        
        writeActionLog(entity, user, "RESUBMIT", "MATERIAL", entity.getId(), "重新提交材料");
        return toResponse(entity);
    }

    @Override
    public List<PartyMaterialSubmissionResponse> listByStudentId(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        validateStudentAccess(user, studentId);
        return submissionRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<PartyMaterialSubmissionResponse> listPendingByClass(String grade, String className) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        List<Long> studentIds = resolveStudentIdsByScope(user, grade, className);
        return submissionRepository.findByStatusInAndStudentIdIn(
                List.of("PENDING", "RETURNED"), studentIds
        ).stream().map(this::toResponse).toList();
    }

    @Override
    public List<PartyActionLogResponse> listActionLogs(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        validateStudentAccess(user, studentId);
        return actionLogRepository.findByStudentIdOrderByOperatedAtDesc(studentId)
                .stream().map(this::toLogResponse).toList();
    }

    @Override
    public List<PartyClassProgressResponse> listClassProgress(String grade, String className) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        List<Long> studentIds = resolveStudentIdsByScope(user, grade, className);
        
        return studentIds.stream().map(studentId -> {
            StudentProfile profile = studentProfileRepository.findById(studentId).orElse(null);
            if (profile == null) return null;
            
            List<PartyMaterialSubmission> submissions = submissionRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
            long pendingCount = submissions.stream().filter(s -> "PENDING".equals(s.getStatus()) || "RETURNED".equals(s.getStatus())).count();
            
            return new PartyClassProgressResponse(
                    studentId,
                    profile.getName(),
                    profile.getStudentNo(),
                    submissions.isEmpty() ? "" : submissions.get(0).getFlowType(),
                    submissions.isEmpty() ? "" : submissions.get(0).getStageName(),
                    submissions.isEmpty() ? "NOT_STARTED" : submissions.get(0).getStatus(),
                    (int) pendingCount,
                    submissions.size()
            );
        }).filter(item -> item != null).toList();
    }

    private void validateStudentAccess(AuthenticatedUser user, Long studentId) {
        if (RoleType.STUDENT.name().equals(user.role())) {
            if (!user.userId().equals(studentId)) {
                throw new BusinessException("学生仅可查看本人数据");
            }
        } else {
            studentDataScopeService.requireStudentAccess(user, studentId);
        }
    }

    private void validateReviewerPermission(AuthenticatedUser user) {
        if (!RoleType.SUPER_ADMIN.name().equals(user.role())
                && !RoleType.COLLEGE_ADMIN.name().equals(user.role())
                && !RoleType.COUNSELOR.name().equals(user.role())) {
            throw new BusinessException("仅管理员/辅导员可审批材料");
        }
    }

    private List<Long> resolveStudentIdsByScope(AuthenticatedUser user, String grade, String className) {
        if (RoleType.SUPER_ADMIN.name().equals(user.role())
                || RoleType.COLLEGE_ADMIN.name().equals(user.role())
                || RoleType.COUNSELOR.name().equals(user.role())) {
            return studentProfileRepository.findAll().stream()
                    .filter(s -> (grade == null || grade.equals(s.getGrade())))
                    .filter(s -> (className == null || className.equals(s.getClassName())))
                    .map(StudentProfile::getId).toList();
        }
        return studentProfileRepository.findAll().stream()
                .filter(s -> (grade == null || grade.equals(s.getGrade())))
                .filter(s -> (className == null || className.equals(s.getClassName())))
                .map(StudentProfile::getId).toList();
    }

    private void writeActionLog(PartyMaterialSubmission entity, AuthenticatedUser operator,
                                String action, String targetType, Long targetId, String detail) {
        PartyActionLog log = new PartyActionLog();
        log.setStudentId(entity.getStudentId());
        log.setFlowType(entity.getFlowType());
        log.setStageName(entity.getStageName());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOperatorId(operator.userId());
        log.setOperatorName(operator.name());
        log.setOperatorRole(operator.role());
        log.setDetail(detail);
        log.setOperatedAt(LocalDateTime.now());
        actionLogRepository.save(log);
    }

    private PartyMaterialSubmissionResponse toResponse(PartyMaterialSubmission entity) {
        return new PartyMaterialSubmissionResponse(
                entity.getId(), entity.getStudentId(), entity.getFlowType(),
                entity.getStageName(), entity.getMaterialType(), entity.getTitle(),
                entity.getContent(), entity.getAttachmentPath(), entity.getStatus(),
                entity.getReviewComment(), entity.getReviewerId(), entity.getReviewerName(),
                entity.getReviewedAt(), entity.getCreatedAt()
        );
    }

    private PartyActionLogResponse toLogResponse(PartyActionLog log) {
        return new PartyActionLogResponse(
                log.getId(), log.getStudentId(), log.getFlowType(), log.getStageName(),
                log.getAction(), log.getTargetType(), log.getTargetId(), log.getOperatorId(),
                log.getOperatorName(), log.getOperatorRole(), log.getDetail(), log.getOperatedAt()
        );
    }
}
