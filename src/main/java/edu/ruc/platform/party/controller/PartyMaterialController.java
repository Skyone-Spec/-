package edu.ruc.platform.party.controller;

import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.security.RequireRoles;
import edu.ruc.platform.party.dto.*;
import edu.ruc.platform.party.service.PartyMaterialApplicationService;
import edu.ruc.platform.party.service.PartyReminderNotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/party-materials")
@RequiredArgsConstructor
public class PartyMaterialController {

    private final PartyMaterialApplicationService partyMaterialService;
    private final PartyReminderNotificationService reminderNotificationService;

    @PostMapping
    @RequireRoles({RoleType.STUDENT, RoleType.LEAGUE_SECRETARY, RoleType.CLASS_LEADER})
    public ApiResponse<PartyMaterialSubmissionResponse> submit(@Valid @RequestBody PartyMaterialSubmitRequest request) {
        return ApiResponse.success("材料提交成功", partyMaterialService.submitMaterial(request));
    }

    @GetMapping("/student/{studentId}")
    @RequireRoles({RoleType.STUDENT, RoleType.LEAGUE_SECRETARY, RoleType.CLASS_LEADER,
            RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.COLLEGE_ADMIN, RoleType.SUPER_ADMIN})
    public ApiResponse<List<PartyMaterialSubmissionResponse>> listByStudent(
            @Positive(message = "学生ID必须大于0") @PathVariable Long studentId) {
        return ApiResponse.success(partyMaterialService.listByStudentId(studentId));
    }

    @GetMapping("/pending")
    @RequireRoles({RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.COLLEGE_ADMIN, RoleType.SUPER_ADMIN})
    public ApiResponse<List<PartyMaterialSubmissionResponse>> listPending(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String className) {
        return ApiResponse.success(partyMaterialService.listPendingByClass(grade, className));
    }

    @PostMapping("/{id}/review")
    @RequireRoles({RoleType.COUNSELOR, RoleType.COLLEGE_ADMIN, RoleType.SUPER_ADMIN})
    public ApiResponse<PartyMaterialSubmissionResponse> review(
            @Positive(message = "提交ID必须大于0") @PathVariable Long id,
            @Valid @RequestBody PartyMaterialReviewRequest request) {
        return ApiResponse.success("审批完成", partyMaterialService.reviewMaterial(id, request));
    }

    @PostMapping("/{id}/withdraw")
    @RequireRoles({RoleType.STUDENT, RoleType.LEAGUE_SECRETARY, RoleType.CLASS_LEADER})
    public ApiResponse<PartyMaterialSubmissionResponse> withdraw(
            @Positive(message = "提交ID必须大于0") @PathVariable Long id) {
        return ApiResponse.success("撤回成功", partyMaterialService.withdrawMaterial(id));
    }

    @PostMapping("/{id}/resubmit")
    @RequireRoles({RoleType.STUDENT, RoleType.LEAGUE_SECRETARY, RoleType.CLASS_LEADER})
    public ApiResponse<PartyMaterialSubmissionResponse> resubmit(
            @Positive(message = "提交ID必须大于0") @PathVariable Long id,
            @Valid @RequestBody PartyMaterialSubmitRequest request) {
        return ApiResponse.success("重新提交成功", partyMaterialService.resubmitMaterial(id, request));
    }

    @GetMapping("/logs/student/{studentId}")
    @RequireRoles({RoleType.STUDENT, RoleType.LEAGUE_SECRETARY, RoleType.CLASS_LEADER,
            RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.COLLEGE_ADMIN, RoleType.SUPER_ADMIN})
    public ApiResponse<List<PartyActionLogResponse>> listLogs(
            @Positive(message = "学生ID必须大于0") @PathVariable Long studentId) {
        return ApiResponse.success(partyMaterialService.listActionLogs(studentId));
    }

    @GetMapping("/class-progress")
    @RequireRoles({RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.COLLEGE_ADMIN, RoleType.SUPER_ADMIN})
    public ApiResponse<List<PartyClassProgressResponse>> classProgress(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String className) {
        return ApiResponse.success(partyMaterialService.listClassProgress(grade, className));
    }

    @GetMapping("/reminders/student/{studentId}")
    @RequireRoles({RoleType.STUDENT, RoleType.LEAGUE_SECRETARY, RoleType.CLASS_LEADER,
            RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.COLLEGE_ADMIN, RoleType.SUPER_ADMIN})
    public ApiResponse<List<PartyReminderNotificationService.ReminderRecord>> listReminders(
            @Positive(message = "学生ID必须大于0") @PathVariable Long studentId) {
        return ApiResponse.success(reminderNotificationService.getReminderHistory(studentId));
    }
}
