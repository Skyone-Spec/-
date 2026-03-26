package edu.ruc.platform.student.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.academic.service.AcademicWarningApplicationService;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.certificate.service.CertificateApplicationService;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.knowledge.service.KnowledgeApplicationService;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.notice.service.NoticeApplicationService;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.party.service.PartyProgressApplicationService;
import edu.ruc.platform.student.dto.StudentDashboardResponse;
import edu.ruc.platform.student.dto.StudentGrowthSuggestionsResponse;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.worklog.service.StudentWorkLogApplicationService;

import java.util.List;

abstract class AbstractStudentSelfService implements StudentSelfApplicationService {

    private final CurrentUserService currentUserService;
    private final StudentProfileApplicationService studentProfileService;
    private final NoticeApplicationService noticeService;
    private final CertificateApplicationService certificateService;
    private final PartyProgressApplicationService partyProgressService;
    private final KnowledgeApplicationService knowledgeService;
    private final AcademicWarningApplicationService academicWarningService;
    private final StudentWorkLogApplicationService studentWorkLogService;
    private final StudentActionRecommendationAssembler recommendationAssembler;

    protected AbstractStudentSelfService(CurrentUserService currentUserService,
                                         StudentProfileApplicationService studentProfileService,
                                         NoticeApplicationService noticeService,
                                         CertificateApplicationService certificateService,
                                         PartyProgressApplicationService partyProgressService,
                                         KnowledgeApplicationService knowledgeService,
                                         AcademicWarningApplicationService academicWarningService,
                                         StudentWorkLogApplicationService studentWorkLogService,
                                         StudentActionRecommendationAssembler recommendationAssembler) {
        this.currentUserService = currentUserService;
        this.studentProfileService = studentProfileService;
        this.noticeService = noticeService;
        this.certificateService = certificateService;
        this.partyProgressService = partyProgressService;
        this.knowledgeService = knowledgeService;
        this.academicWarningService = academicWarningService;
        this.studentWorkLogService = studentWorkLogService;
        this.recommendationAssembler = recommendationAssembler;
    }

    @Override
    public StudentDashboardResponse dashboard() {
        StudentSelfSnapshot snapshot = loadDashboardSnapshot();
        return new StudentDashboardResponse(
                snapshot.profile(),
                snapshot.partyProgress(),
                snapshot.reminders(),
                snapshot.notices(),
                snapshot.certificateRequests(),
                snapshot.recommendedKnowledge(),
                recommendationAssembler.buildFocusItems(
                        snapshot.reminders(),
                        snapshot.notices(),
                        snapshot.certificateRequests(),
                        snapshot.academicAnalysis(),
                        snapshot.worklogSummary(),
                        snapshot.portrait())
        );
    }

    @Override
    public StudentGrowthSuggestionsResponse growthSuggestions() {
        StudentSelfSnapshot snapshot = loadGrowthSnapshot();
        StudentProfileResponse profile = snapshot.profile();
        return new StudentGrowthSuggestionsResponse(
                profile.id(),
                profile.name(),
                recommendationAssembler.buildGrowthSuggestions(
                        snapshot.academicAnalysis(),
                        snapshot.worklogSummary(),
                        snapshot.portrait(),
                        snapshot.certificateRequests())
        );
    }

    @Override
    public List<TargetedNoticeResponse> myNotices() {
        return noticeService.listTargetedNotices(currentStudentId());
    }

    @Override
    public List<CertificateRequestResponse> myCertificateRequests() {
        return certificateService.listByStudentId(currentStudentId());
    }

    @Override
    public PartyProgressResponse myPartyProgress() {
        return partyProgressService.getProgress(currentStudentId());
    }

    @Override
    public List<ReminderResponse> myPartyReminders() {
        return partyProgressService.listReminders(currentStudentId());
    }

    @Override
    public List<KnowledgeSearchResponse> recommendedKnowledge() {
        return knowledgeService.recommendForStudent(currentStudentId());
    }

    protected StudentSelfSnapshot loadDashboardSnapshot() {
        StudentProfileResponse profile = studentProfileService.currentStudentProfile();
        return buildSnapshot(profile, true, true);
    }

    protected StudentSelfSnapshot loadGrowthSnapshot() {
        StudentProfileResponse profile = studentProfileService.currentStudentProfile();
        return buildSnapshot(profile, false, true);
    }

    private StudentSelfSnapshot buildSnapshot(StudentProfileResponse profile,
                                             boolean includeDashboardLists,
                                             boolean includeGrowthAnalysis) {
        Long studentId = profile.id();
        List<ReminderResponse> reminders = includeDashboardLists ? myPartyReminders() : List.of();
        List<TargetedNoticeResponse> notices = includeDashboardLists ? myNotices().stream().limit(3).toList() : List.of();
        List<KnowledgeSearchResponse> knowledge = includeDashboardLists ? recommendedKnowledge() : List.of();
        List<CertificateRequestResponse> certificates = myCertificateRequests();
        return new StudentSelfSnapshot(
                profile,
                includeDashboardLists ? myPartyProgress() : null,
                reminders,
                notices,
                certificates,
                knowledge,
                includeGrowthAnalysis ? academicWarningService.analyze(studentId) : null,
                includeGrowthAnalysis ? studentWorkLogService.summarize(studentId) : null,
                includeGrowthAnalysis ? studentProfileService.getPortrait(studentId) : null
        );
    }

    protected Long currentStudentId() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return user.userId();
    }
}
