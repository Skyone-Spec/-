package edu.ruc.platform.party.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockPartyMaterialService implements PartyMaterialApplicationService {

    private final CurrentUserService currentUserService;
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, PartyMaterialSubmissionResponse> submissions = new ConcurrentHashMap<>();
    private final List<PartyActionLogResponse> actionLogs = new ArrayList<>();

    @Override
    public PartyMaterialSubmissionResponse submitMaterial(PartyMaterialSubmitRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        Long id = idGenerator.incrementAndGet();
        PartyMaterialSubmissionResponse response = new PartyMaterialSubmissionResponse(
                id, request.studentId(), request.flowType(), request.stageName(),
                request.materialType(), request.title(), request.content(),
                request.attachmentPath(), "PENDING", null, null, null, null, LocalDateTime.now()
        );
        submissions.put(id, response);
        addLog(request.studentId(), request.flowType(), request.stageName(), "SUBMIT", "MATERIAL", id, "提交材料");
        return response;
    }

    @Override
    public PartyMaterialSubmissionResponse reviewMaterial(Long submissionId, PartyMaterialReviewRequest request) {
        PartyMaterialSubmissionResponse existing = submissions.get(submissionId);
        if (existing == null) throw new BusinessException("材料提交记录不存在");
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        
        String action = request.action().trim().toLowerCase();
        String nextStatus = switch (action) {
            case "approve" -> "APPROVED";
            case "reject" -> "REJECTED";
            case "return" -> "RETURNED";
            default -> throw new BusinessException("审批动作仅支持 approve、reject 或 return");
        };
        
        PartyMaterialSubmissionResponse updated = new PartyMaterialSubmissionResponse(
                existing.id(), existing.studentId(), existing.flowType(), existing.stageName(),
                existing.materialType(), existing.title(), existing.content(), existing.attachmentPath(),
                nextStatus, request.reviewComment(), user.userId(), user.name(), LocalDateTime.now(), existing.createdAt()
        );
        submissions.put(submissionId, updated);
        addLog(existing.studentId(), existing.flowType(), existing.stageName(), action.toUpperCase(), "MATERIAL", submissionId, request.reviewComment());
        return updated;
    }

    @Override
    public PartyMaterialSubmissionResponse withdrawMaterial(Long submissionId) {
        PartyMaterialSubmissionResponse existing = submissions.get(submissionId);
        if (existing == null) throw new BusinessException("材料提交记录不存在");
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        
        if (!"PENDING".equals(existing.status()) && !"RETURNED".equals(existing.status())) {
            throw new BusinessException("当前状态不允许撤回");
        }
        
        PartyMaterialSubmissionResponse updated = new PartyMaterialSubmissionResponse(
                existing.id(), existing.studentId(), existing.flowType(), existing.stageName(),
                existing.materialType(), existing.title(), existing.content(), existing.attachmentPath(),
                "WITHDRAWN", existing.reviewComment(), existing.reviewerId(), existing.reviewerName(),
                existing.reviewedAt(), existing.createdAt()
        );
        submissions.put(submissionId, updated);
        addLog(existing.studentId(), existing.flowType(), existing.stageName(), "WITHDRAW", "MATERIAL", submissionId, "撤回材料提交");
        return updated;
    }

    @Override
    public PartyMaterialSubmissionResponse resubmitMaterial(Long submissionId, PartyMaterialSubmitRequest request) {
        PartyMaterialSubmissionResponse existing = submissions.get(submissionId);
        if (existing == null) throw new BusinessException("材料提交记录不存在");
        
        if (!"RETURNED".equals(existing.status()) && !"WITHDRAWN".equals(existing.status())) {
            throw new BusinessException("当前状态不允许重新提交");
        }
        
        PartyMaterialSubmissionResponse updated = new PartyMaterialSubmissionResponse(
                existing.id(), existing.studentId(), existing.flowType(), existing.stageName(),
                request.materialType(), request.title(), request.content(), request.attachmentPath(),
                "PENDING", null, null, null, null, existing.createdAt()
        );
        submissions.put(submissionId, updated);
        addLog(existing.studentId(), existing.flowType(), existing.stageName(), "RESUBMIT", "MATERIAL", submissionId, "重新提交材料");
        return updated;
    }

    @Override
    public List<PartyMaterialSubmissionResponse> listByStudentId(Long studentId) {
        return submissions.values().stream()
                .filter(s -> s.studentId().equals(studentId))
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .toList();
    }

    @Override
    public List<PartyMaterialSubmissionResponse> listPendingByClass(String grade, String className) {
        return submissions.values().stream()
                .filter(s -> "PENDING".equals(s.status()) || "RETURNED".equals(s.status()))
                .toList();
    }

    @Override
    public List<PartyActionLogResponse> listActionLogs(Long studentId) {
        return actionLogs.stream()
                .filter(log -> log.studentId().equals(studentId))
                .sorted((a, b) -> b.operatedAt().compareTo(a.operatedAt()))
                .toList();
    }

    @Override
    public List<PartyClassProgressResponse> listClassProgress(String grade, String className) {
        return List.of();
    }

    private void addLog(Long studentId, String flowType, String stageName, String action,
                        String targetType, Long targetId, String detail) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        actionLogs.add(new PartyActionLogResponse(
                (long) actionLogs.size() + 1, studentId, flowType, stageName, action,
                targetType, targetId, user.userId(), user.name(), user.role(), detail, LocalDateTime.now()
        ));
    }
}
