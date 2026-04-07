package edu.ruc.platform.party.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.PartyStageTimelineResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.party.service.PartyProgressApplicationService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/party-progress")
@RequiredArgsConstructor
public class PartyProgressController {

    private final PartyProgressApplicationService partyProgressService;
    private final CurrentUserService currentUserService;

    @GetMapping("/{studentId}")
    public ApiResponse<PartyProgressResponse> getByStudentId(@Positive(message = "学生ID必须大于 0") @PathVariable Long studentId) {
        currentUserService.requireStudentAccess(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(partyProgressService.getProgress(studentId));
    }

    @GetMapping("/{studentId}/timeline")
    public ApiResponse<PartyStageTimelineResponse> getTimeline(@Positive(message = "学生ID必须大于 0") @PathVariable Long studentId) {
        currentUserService.requireStudentAccess(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(partyProgressService.getTimeline(studentId));
    }

    @GetMapping("/{studentId}/reminders")
    public ApiResponse<List<ReminderResponse>> listReminders(@Positive(message = "学生ID必须大于 0") @PathVariable Long studentId) {
        currentUserService.requireStudentAccess(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(partyProgressService.listReminders(studentId));
    }
}
