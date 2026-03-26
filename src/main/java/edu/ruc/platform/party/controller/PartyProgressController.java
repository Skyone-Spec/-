package edu.ruc.platform.party.controller;

import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.PartyStageTimelineResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.party.service.PartyProgressApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/party-progress")
@RequiredArgsConstructor
public class PartyProgressController {

    private final PartyProgressApplicationService partyProgressService;

    @GetMapping("/{studentId}")
    public ApiResponse<PartyProgressResponse> getByStudentId(@PathVariable Long studentId) {
        return ApiResponse.success(partyProgressService.getProgress(studentId));
    }

    @GetMapping("/{studentId}/timeline")
    public ApiResponse<PartyStageTimelineResponse> getTimeline(@PathVariable Long studentId) {
        return ApiResponse.success(partyProgressService.getTimeline(studentId));
    }

    @GetMapping("/{studentId}/reminders")
    public ApiResponse<List<ReminderResponse>> listReminders(@PathVariable Long studentId) {
        return ApiResponse.success(partyProgressService.listReminders(studentId));
    }
}
