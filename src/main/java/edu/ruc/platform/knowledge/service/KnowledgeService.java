package edu.ruc.platform.knowledge.service;

import edu.ruc.platform.admin.dto.KnowledgeAttachmentResponse;
import edu.ruc.platform.admin.repository.KnowledgeAttachmentRepository;
import edu.ruc.platform.certificate.repository.CertificateRequestRepository;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.knowledge.dto.KnowledgeDetailResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.knowledge.repository.KnowledgeDocumentRepository;
import edu.ruc.platform.notice.domain.Notice;
import edu.ruc.platform.notice.repository.NoticeRepository;
import edu.ruc.platform.party.domain.PartyProgressRecord;
import edu.ruc.platform.party.repository.PartyProgressRecordRepository;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class KnowledgeService implements KnowledgeApplicationService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final KnowledgeAttachmentRepository knowledgeAttachmentRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NoticeRepository noticeRepository;
    private final CertificateRequestRepository certificateRequestRepository;
    private final PartyProgressRecordRepository partyProgressRecordRepository;

    @Override
    public List<KnowledgeSearchResponse> search(String keyword) {
        return knowledgeDocumentRepository.findAll()
                .stream()
                .filter(doc -> Boolean.TRUE.equals(doc.getPublished()))
                .filter(doc -> containsIgnoreCase(doc.getTitle(), keyword)
                        || containsIgnoreCase(doc.getCategory(), keyword)
                        || containsIgnoreCase(doc.getContent(), keyword))
                .map(this::toSafeSearchResponse)
                .toList();
    }

    @Override
    public List<KnowledgeSearchResponse> listTemplates() {
        return List.of(
                new KnowledgeSearchResponse(101L, "在读证明模板", "模板下载", "/templates/study-certificate.docx", "标准在读证明模板下载", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(102L, "请假申请表", "模板下载", "/templates/leave-request.docx", "标准请假申请表模板下载", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(103L, "活动预算表", "模板下载", "/templates/activity-budget.xlsx", "活动预算表下载", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(104L, "知识库导入模板", "模板下载", "/templates/knowledge-import.xlsx", "管理员批量导入知识条目模板", "STANDARD_ANSWER", false)
        );
    }

    @Override
    public KnowledgeDetailResponse getDetail(Long id) {
        var doc = knowledgeDocumentRepository.findById(id)
                .filter(item -> Boolean.TRUE.equals(item.getPublished()))
                .orElseThrow(() -> new BusinessException("知识条目不存在"));
        List<KnowledgeAttachmentResponse> attachments = knowledgeAttachmentRepository.findByKnowledgeIdOrderByCreatedAtDesc(id).stream()
                .map(item -> new KnowledgeAttachmentResponse(
                        item.getId(),
                        item.getKnowledgeId(),
                        item.getFileName(),
                        item.getContentType(),
                        item.getFileSize(),
                        item.getStoragePath(),
                        item.getUploadedBy(),
                        item.getCreatedAt()
                ))
                .toList();
        List<KnowledgeSearchResponse> relatedItems = knowledgeDocumentRepository.findAll().stream()
                .filter(item -> !item.getId().equals(id))
                .filter(item -> Boolean.TRUE.equals(item.getPublished()))
                .filter(item -> java.util.Objects.equals(item.getCategory(), doc.getCategory()))
                .limit(3)
                .map(this::toSafeSearchResponse)
                .toList();
        return new KnowledgeDetailResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getCategory(),
                doc.getOfficialUrl(),
                resolveAnswer(doc),
                resolveResponseStrategy(doc),
                isOfficialLinkOnly(doc),
                buildSafetyTip(doc),
                doc.getSourceFileName(),
                doc.getAudienceScope(),
                attachments,
                relatedItems
        );
    }

    @Override
    public List<KnowledgeSearchResponse> recommendForStudent(Long studentId) {
        StudentProfile profile = studentProfileRepository.findById(studentId).orElse(null);
        List<Notice> notices = noticeRepository.findAllByOrderByPublishTimeDesc();
        List<String> certificateStatuses = certificateRequestRepository.findByStudentId(studentId).stream()
                .map(item -> item.getStatus() == null ? "" : item.getStatus())
                .toList();
        PartyProgressRecord progress = partyProgressRecordRepository.findByStudentId(studentId).orElse(null);
        return knowledgeDocumentRepository.findAll()
                .stream()
                .filter(doc -> Boolean.TRUE.equals(doc.getPublished()))
                .sorted(Comparator
                        .comparingInt((edu.ruc.platform.knowledge.domain.KnowledgeDocument doc) -> scoreRecommendation(doc, profile, notices, certificateStatuses, progress))
                        .reversed()
                        .thenComparing(edu.ruc.platform.knowledge.domain.KnowledgeDocument::getId))
                .limit(3)
                .map(this::toSafeSearchResponse)
                .toList();
    }

    private int scoreRecommendation(edu.ruc.platform.knowledge.domain.KnowledgeDocument doc,
                                    StudentProfile profile,
                                    List<Notice> notices,
                                    List<String> certificateStatuses,
                                    PartyProgressRecord progress) {
        int score = 0;
        String text = ((doc.getTitle() == null ? "" : doc.getTitle()) + " "
                + (doc.getCategory() == null ? "" : doc.getCategory()) + " "
                + (doc.getContent() == null ? "" : doc.getContent())).toLowerCase(Locale.ROOT);
        if (profile != null && profile.getMajor() != null && text.contains(profile.getMajor().toLowerCase(Locale.ROOT))) {
            score += 2;
        }
        boolean hasPartyNotice = notices.stream()
                .anyMatch(notice -> notice.getTag() != null && (notice.getTag().contains("党团") || notice.getTag().contains("流程")));
        if ((progress != null && progress.getCurrentStage() != null) || hasPartyNotice) {
            if (text.contains("党") || text.contains("团")) {
                score += 3;
            }
        }
        boolean hasCertificateDemand = certificateStatuses.stream()
                .anyMatch(status -> "PENDING".equalsIgnoreCase(status) || "WITHDRAWN".equalsIgnoreCase(status));
        if (hasCertificateDemand && (text.contains("证明") || text.contains("表格"))) {
            score += 4;
        }
        if (text.contains("数据安全") || text.contains("保密")) {
            score += 1;
        }
        return score;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && keyword != null && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private KnowledgeSearchResponse toSafeSearchResponse(edu.ruc.platform.knowledge.domain.KnowledgeDocument doc) {
        return new KnowledgeSearchResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getCategory(),
                doc.getOfficialUrl(),
                resolveAnswer(doc),
                resolveResponseStrategy(doc),
                isOfficialLinkOnly(doc)
        );
    }

    private String resolveAnswer(edu.ruc.platform.knowledge.domain.KnowledgeDocument doc) {
        if (isOfficialLinkOnly(doc)) {
            return "该主题涉及敏感信息或权限边界，前台仅展示公开口径，请优先查看官方链接或联系负责老师。";
        }
        return doc.getContent();
    }

    private String resolveResponseStrategy(edu.ruc.platform.knowledge.domain.KnowledgeDocument doc) {
        return isOfficialLinkOnly(doc) ? "OFFICIAL_LINK_ONLY" : "STANDARD_ANSWER";
    }

    private boolean isOfficialLinkOnly(edu.ruc.platform.knowledge.domain.KnowledgeDocument doc) {
        return "数据安全".equals(doc.getCategory())
                || containsIgnoreCase(doc.getTitle(), "保密")
                || containsIgnoreCase(doc.getContent(), "严格控制");
    }

    private String buildSafetyTip(edu.ruc.platform.knowledge.domain.KnowledgeDocument doc) {
        if (isOfficialLinkOnly(doc)) {
            return "敏感字段不在知识库详情中直接展开，需按权限走官方渠道查询。";
        }
        return "当前回复优先采用知识库标准答案，如遇特殊场景请以学院正式通知为准。";
    }
}
