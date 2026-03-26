package edu.ruc.platform.student.service;

import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.student.dto.StudentDashboardResponse;
import edu.ruc.platform.student.dto.StudentGrowthSuggestionsResponse;

import java.util.List;

public interface StudentSelfApplicationService {

    StudentDashboardResponse dashboard();

    StudentGrowthSuggestionsResponse growthSuggestions();

    List<TargetedNoticeResponse> myNotices();

    List<CertificateRequestResponse> myCertificateRequests();

    PartyProgressResponse myPartyProgress();

    List<ReminderResponse> myPartyReminders();

    List<KnowledgeSearchResponse> recommendedKnowledge();
}
