package edu.ruc.platform.knowledge.service;

import edu.ruc.platform.admin.repository.KnowledgeAttachmentRepository;
import edu.ruc.platform.certificate.domain.CertificateRequest;
import edu.ruc.platform.certificate.repository.CertificateRequestRepository;
import edu.ruc.platform.knowledge.domain.KnowledgeDocument;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.knowledge.repository.KnowledgeDocumentRepository;
import edu.ruc.platform.notice.domain.Notice;
import edu.ruc.platform.notice.repository.NoticeRepository;
import edu.ruc.platform.party.domain.PartyProgressRecord;
import edu.ruc.platform.party.repository.PartyProgressRecordRepository;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KnowledgeServiceUnitTest {

    @Test
    void nonMockTemplatesAndRecommendationsMatchMockContractShape() {
        KnowledgeDocumentRepository knowledgeDocumentRepository = mock(KnowledgeDocumentRepository.class);
        KnowledgeAttachmentRepository knowledgeAttachmentRepository = mock(KnowledgeAttachmentRepository.class);
        StudentProfileRepository studentProfileRepository = mock(StudentProfileRepository.class);
        NoticeRepository noticeRepository = mock(NoticeRepository.class);
        CertificateRequestRepository certificateRequestRepository = mock(CertificateRequestRepository.class);
        PartyProgressRecordRepository partyProgressRecordRepository = mock(PartyProgressRecordRepository.class);

        KnowledgeService service = new KnowledgeService(
                knowledgeDocumentRepository,
                knowledgeAttachmentRepository,
                studentProfileRepository,
                noticeRepository,
                certificateRequestRepository,
                partyProgressRecordRepository
        );

        StudentProfile profile = new StudentProfile();
        profile.setId(10001L);
        profile.setMajor("计算机类");

        Notice notice = new Notice();
        notice.setId(1L);
        notice.setTitle("党团工作提醒");
        notice.setSummary("请关注组织流程");
        notice.setTag("党团事务,流程");
        notice.setPublishTime(LocalDateTime.now());

        CertificateRequest certificateRequest = new CertificateRequest();
        certificateRequest.setId(2001L);
        certificateRequest.setStudentId(10001L);
        certificateRequest.setStatus("PENDING");

        PartyProgressRecord progressRecord = new PartyProgressRecord();
        progressRecord.setId(3001L);
        progressRecord.setStudentId(10001L);
        progressRecord.setCurrentStage("积极分子");
        progressRecord.setStageStartDate(LocalDate.now().minusDays(30));

        when(studentProfileRepository.findById(10001L)).thenReturn(Optional.of(profile));
        when(noticeRepository.findAllByOrderByPublishTimeDesc()).thenReturn(List.of(notice));
        when(certificateRequestRepository.findByStudentId(10001L)).thenReturn(List.of(certificateRequest));
        when(partyProgressRecordRepository.findByStudentId(10001L)).thenReturn(Optional.of(progressRecord));
        when(knowledgeDocumentRepository.findAll()).thenReturn(List.of(
                knowledgeDoc(1L, "电子证明与常用表格入口", "办事指南", "证明 表格 在读证明"),
                knowledgeDoc(2L, "党员发展流程说明", "党团事务", "党 团 积极分子 流程"),
                knowledgeDoc(3L, "计算机类成长建议", "学业发展", "计算机类 学业 规划"),
                knowledgeDoc(4L, "数据安全提醒", "数据安全", "保密 严格控制"),
                knowledgeDoc(5L, "普通通知解读", "通知公告", "日常事务")
        ));

        List<KnowledgeSearchResponse> templates = service.listTemplates();
        List<KnowledgeSearchResponse> recommendations = service.recommendForStudent(10001L);

        assertEquals(5, templates.size());
        assertEquals("党团事务材料模板", templates.get(3).title());
        assertEquals(4, recommendations.size());
        assertTrue(recommendations.stream().anyMatch(item -> "电子证明与常用表格入口".equals(item.title())));
    }

    private KnowledgeDocument knowledgeDoc(Long id, String title, String category, String content) {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(id);
        document.setTitle(title);
        document.setCategory(category);
        document.setContent(content);
        document.setOfficialUrl("https://example.edu/" + id);
        document.setPublished(true);
        return document;
    }
}
