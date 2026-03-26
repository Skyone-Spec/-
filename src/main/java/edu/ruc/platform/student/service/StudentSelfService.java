package edu.ruc.platform.student.service;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.academic.service.AcademicWarningApplicationService;
import edu.ruc.platform.certificate.service.CertificateApplicationService;
import edu.ruc.platform.knowledge.service.KnowledgeApplicationService;
import edu.ruc.platform.notice.service.NoticeApplicationService;
import edu.ruc.platform.party.service.PartyProgressApplicationService;
import edu.ruc.platform.worklog.service.StudentWorkLogApplicationService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!mock")
public class StudentSelfService extends AbstractStudentSelfService {

    public StudentSelfService(CurrentUserService currentUserService,
                              StudentProfileApplicationService studentProfileService,
                              NoticeApplicationService noticeService,
                              CertificateApplicationService certificateService,
                              PartyProgressApplicationService partyProgressService,
                              KnowledgeApplicationService knowledgeService,
                              AcademicWarningApplicationService academicWarningService,
                              StudentWorkLogApplicationService studentWorkLogService,
                              StudentActionRecommendationAssembler recommendationAssembler) {
        super(
                currentUserService,
                studentProfileService,
                noticeService,
                certificateService,
                partyProgressService,
                knowledgeService,
                academicWarningService,
                studentWorkLogService,
                recommendationAssembler
        );
    }
}
