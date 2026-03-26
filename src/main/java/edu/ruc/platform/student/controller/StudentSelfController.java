package edu.ruc.platform.student.controller;

import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.student.dto.StudentDashboardResponse;
import edu.ruc.platform.student.dto.StudentGrowthSuggestionsResponse;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.service.StudentSelfApplicationService;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentSelfController {

    private final StudentProfileApplicationService studentProfileService;
    private final StudentSelfApplicationService studentSelfService;

    @GetMapping("/me")
    public ApiResponse<StudentProfileResponse> me() {
        return ApiResponse.success(studentProfileService.currentStudentProfile());
    }

    @GetMapping("/dashboard")
    public ApiResponse<StudentDashboardResponse> dashboard() {
        return ApiResponse.success(studentSelfService.dashboard());
    }

    @GetMapping("/growth-suggestions")
    public ApiResponse<StudentGrowthSuggestionsResponse> growthSuggestions() {
        return ApiResponse.success(studentSelfService.growthSuggestions());
    }

    @GetMapping("/notices")
    public ApiResponse<List<TargetedNoticeResponse>> myNotices() {
        return ApiResponse.success(studentSelfService.myNotices());
    }

    @GetMapping("/certificates/requests")
    public ApiResponse<List<CertificateRequestResponse>> myCertificateRequests() {
        return ApiResponse.success(studentSelfService.myCertificateRequests());
    }

    @GetMapping("/party-progress")
    public ApiResponse<PartyProgressResponse> myPartyProgress() {
        return ApiResponse.success(studentSelfService.myPartyProgress());
    }

    @GetMapping("/party-progress/reminders")
    public ApiResponse<List<ReminderResponse>> myPartyReminders() {
        return ApiResponse.success(studentSelfService.myPartyReminders());
    }

    @GetMapping("/knowledge/recommended")
    public ApiResponse<List<KnowledgeSearchResponse>> recommendedKnowledge() {
        return ApiResponse.success(studentSelfService.recommendedKnowledge());
    }
}
