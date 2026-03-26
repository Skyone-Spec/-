package edu.ruc.platform.student.service;

import edu.ruc.platform.academic.dto.AcademicAnalysisResponse;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.student.dto.StudentPortraitResponse;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.worklog.dto.StudentWorkloadSummaryResponse;

import java.util.List;

public record StudentSelfSnapshot(
        StudentProfileResponse profile,
        PartyProgressResponse partyProgress,
        List<ReminderResponse> reminders,
        List<TargetedNoticeResponse> notices,
        List<CertificateRequestResponse> certificateRequests,
        List<KnowledgeSearchResponse> recommendedKnowledge,
        AcademicAnalysisResponse academicAnalysis,
        StudentWorkloadSummaryResponse worklogSummary,
        StudentPortraitResponse portrait
) {
}
