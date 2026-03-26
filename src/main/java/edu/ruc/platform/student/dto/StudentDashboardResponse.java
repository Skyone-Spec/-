package edu.ruc.platform.student.dto;

import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.ReminderResponse;

import java.util.List;

public record StudentDashboardResponse(
        StudentProfileResponse profile,
        PartyProgressResponse partyProgress,
        List<ReminderResponse> reminders,
        List<TargetedNoticeResponse> notices,
        List<CertificateRequestResponse> certificateRequests,
        List<KnowledgeSearchResponse> recommendedKnowledge,
        List<StudentDashboardFocusItemResponse> focusItems
) {
}
