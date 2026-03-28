package edu.ruc.platform.knowledge.service;

import edu.ruc.platform.admin.dto.KnowledgeAttachmentResponse;
import edu.ruc.platform.certificate.repository.CertificateRequestRepository;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.knowledge.domain.KnowledgeDocument;
import edu.ruc.platform.knowledge.domain.LatestCertTemplate;
import edu.ruc.platform.knowledge.domain.LatestFileObject;
import edu.ruc.platform.knowledge.domain.LatestKnowledgePolicy;
import edu.ruc.platform.knowledge.dto.KnowledgeDetailResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.knowledge.repository.LatestCertTemplateRepository;
import edu.ruc.platform.knowledge.repository.LatestFileObjectRepository;
import edu.ruc.platform.knowledge.repository.LatestKnowledgePolicyRepository;
import edu.ruc.platform.notice.domain.Notice;
import edu.ruc.platform.notice.repository.NoticeRepository;
import edu.ruc.platform.party.domain.PartyProgressRecord;
import edu.ruc.platform.party.repository.PartyProgressRecordRepository;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Profile("kingbase")
@RequiredArgsConstructor
public class KingbaseKnowledgeService implements KnowledgeApplicationService {

    private final LatestKnowledgePolicyRepository latestKnowledgePolicyRepository;
    private final LatestFileObjectRepository latestFileObjectRepository;
    private final LatestCertTemplateRepository latestCertTemplateRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NoticeRepository noticeRepository;
    private final CertificateRequestRepository certificateRequestRepository;
    private final PartyProgressRecordRepository partyProgressRecordRepository;

    @Override
    public List<KnowledgeSearchResponse> search(String keyword) {
        String normalizedKeyword = QueryFilterSupport.trimToNull(keyword);
        if (normalizedKeyword == null) {
            return List.of();
        }
        return latestKnowledgePolicyRepository.findByIsDeletedAndIsPublished(0, 1).stream()
                .filter(item -> QueryFilterSupport.containsIgnoreCase(item.getTitle(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getSummary(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getContent(), normalizedKeyword))
                .map(this::toSearchResponse)
                .toList();
    }

    @Override
    public List<KnowledgeSearchResponse> listTemplates() {
        return latestCertTemplateRepository.findByIsDeletedAndIsActive(0, 1).stream()
                .sorted(Comparator.comparing(LatestCertTemplate::getId))
                .map(template -> {
                    LatestFileObject file = latestFileObjectRepository.findById(template.getFileId()).orElse(null);
                    String url = file == null ? null : file.getStoragePath();
                    return new KnowledgeSearchResponse(
                            template.getId(),
                            template.getTemplateName(),
                            "模板下载",
                            url,
                            template.getTemplateName() + " 下载",
                            "STANDARD_ANSWER",
                            false
                    );
                })
                .toList();
    }

    @Override
    public KnowledgeDetailResponse getDetail(Long id) {
        LatestKnowledgePolicy policy = latestKnowledgePolicyRepository.findById(id)
                .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
                .filter(item -> item.getIsPublished() != null && item.getIsPublished() == 1)
                .orElseThrow(() -> new BusinessException("知识条目不存在"));
        LatestFileObject file = policy.getAttachmentFileId() == null
                ? null
                : latestFileObjectRepository.findById(policy.getAttachmentFileId()).orElse(null);
        List<KnowledgeAttachmentResponse> attachments = file == null
                ? List.of()
                : List.of(new KnowledgeAttachmentResponse(
                        file.getId(),
                        policy.getId(),
                        file.getOriginalName(),
                        file.getMimeType(),
                        file.getSizeBytes(),
                        file.getStoragePath(),
                        "system",
                        LocalDateTime.now()
                ));
        List<KnowledgeSearchResponse> relatedItems = latestKnowledgePolicyRepository.findByIsDeletedAndIsPublished(0, 1).stream()
                .filter(item -> !item.getId().equals(id))
                .limit(3)
                .map(this::toSearchResponse)
                .toList();
        String sourceFileName = file == null ? null : file.getOriginalName();
        return new KnowledgeDetailResponse(
                policy.getId(),
                policy.getTitle(),
                resolveCategory(policy),
                policy.getSourceUrl(),
                resolveAnswer(policy),
                resolveResponseStrategy(policy),
                isOfficialLinkOnly(policy),
                buildSafetyTip(policy),
                sourceFileName,
                "ALL",
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
        return latestKnowledgePolicyRepository.findByIsDeletedAndIsPublished(0, 1).stream()
                .sorted(Comparator
                        .comparingInt((LatestKnowledgePolicy item) -> scoreRecommendation(item, profile, notices, certificateStatuses, progress))
                        .reversed()
                        .thenComparing(LatestKnowledgePolicy::getId))
                .limit(4)
                .map(this::toSearchResponse)
                .toList();
    }

    private KnowledgeSearchResponse toSearchResponse(LatestKnowledgePolicy policy) {
        return new KnowledgeSearchResponse(
                policy.getId(),
                policy.getTitle(),
                resolveCategory(policy),
                policy.getSourceUrl(),
                resolveAnswer(policy),
                resolveResponseStrategy(policy),
                isOfficialLinkOnly(policy)
        );
    }

    private String resolveCategory(LatestKnowledgePolicy policy) {
        String text = ((policy.getTitle() == null ? "" : policy.getTitle()) + " "
                + (policy.getSummary() == null ? "" : policy.getSummary()) + " "
                + (policy.getContent() == null ? "" : policy.getContent())).toLowerCase(Locale.ROOT);
        if (text.contains("党") || text.contains("团")) {
            return "党团事务";
        }
        if (text.contains("奖学金") || text.contains("评优")) {
            return "奖助学金";
        }
        if (text.contains("培养方案") || text.contains("课程替代") || text.contains("学业")) {
            return "学业发展";
        }
        return "政策知识";
    }

    private String resolveAnswer(LatestKnowledgePolicy policy) {
        if (isOfficialLinkOnly(policy)) {
            return "该主题涉及敏感信息或权限边界，请优先查看官方链接或联系负责老师。";
        }
        if (QueryFilterSupport.trimToNull(policy.getContent()) != null) {
            return policy.getContent();
        }
        return policy.getSummary();
    }

    private String resolveResponseStrategy(LatestKnowledgePolicy policy) {
        return isOfficialLinkOnly(policy) ? "OFFICIAL_LINK_ONLY" : "STANDARD_ANSWER";
    }

    private boolean isOfficialLinkOnly(LatestKnowledgePolicy policy) {
        String text = ((policy.getTitle() == null ? "" : policy.getTitle()) + " "
                + (policy.getSummary() == null ? "" : policy.getSummary()) + " "
                + (policy.getContent() == null ? "" : policy.getContent())).toLowerCase(Locale.ROOT);
        return text.contains("保密") || text.contains("严格控制") || text.contains("敏感");
    }

    private String buildSafetyTip(LatestKnowledgePolicy policy) {
        if (isOfficialLinkOnly(policy)) {
            return "敏感信息不在知识库详情中直接展开，需按权限走官方渠道查询。";
        }
        return "当前回复优先采用知识库标准答案，如遇特殊场景请以学院正式通知为准。";
    }

    private int scoreRecommendation(LatestKnowledgePolicy policy,
                                    StudentProfile profile,
                                    List<Notice> notices,
                                    List<String> certificateStatuses,
                                    PartyProgressRecord progress) {
        KnowledgeDocument proxy = new KnowledgeDocument();
        proxy.setId(policy.getId());
        proxy.setTitle(policy.getTitle());
        proxy.setCategory(resolveCategory(policy));
        proxy.setContent(resolveAnswer(policy));
        proxy.setOfficialUrl(policy.getSourceUrl());
        proxy.setPublished(policy.getIsPublished() != null && policy.getIsPublished() == 1);
        return scoreProxy(proxy, profile, notices, certificateStatuses, progress);
    }

    private int scoreProxy(KnowledgeDocument doc,
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
}
