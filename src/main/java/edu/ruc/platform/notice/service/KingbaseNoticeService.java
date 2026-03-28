package edu.ruc.platform.notice.service;

import edu.ruc.platform.auth.domain.LatestStudentExt;
import edu.ruc.platform.auth.domain.LatestUser;
import edu.ruc.platform.auth.repository.LatestStudentExtRepository;
import edu.ruc.platform.auth.repository.LatestUserRepository;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.notice.domain.LatestNoticeDelivery;
import edu.ruc.platform.notice.domain.LatestNoticeDeliveryTarget;
import edu.ruc.platform.notice.domain.LatestNoticeItem;
import edu.ruc.platform.notice.domain.LatestNoticeItemTag;
import edu.ruc.platform.notice.domain.LatestNoticeTagDict;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.notice.repository.LatestNoticeDeliveryRepository;
import edu.ruc.platform.notice.repository.LatestNoticeDeliveryTargetRepository;
import edu.ruc.platform.notice.repository.LatestNoticeItemRepository;
import edu.ruc.platform.notice.repository.LatestNoticeItemTagRepository;
import edu.ruc.platform.notice.repository.LatestNoticeTagDictRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile("kingbase")
@RequiredArgsConstructor
public class KingbaseNoticeService implements NoticeApplicationService {

    private final LatestNoticeItemRepository latestNoticeItemRepository;
    private final LatestNoticeItemTagRepository latestNoticeItemTagRepository;
    private final LatestNoticeTagDictRepository latestNoticeTagDictRepository;
    private final LatestNoticeDeliveryRepository latestNoticeDeliveryRepository;
    private final LatestNoticeDeliveryTargetRepository latestNoticeDeliveryTargetRepository;
    private final LatestUserRepository latestUserRepository;
    private final LatestStudentExtRepository latestStudentExtRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<TargetedNoticeResponse> listTargetedNotices(Long studentId) {
        LatestUser user = latestUserRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("未找到学生信息"));
        LatestStudentExt ext = latestStudentExtRepository.findByStudentNoAndIsDeleted(user.getStudentNo(), 0).orElse(null);
        StudentNoticeSnapshot snapshot = new StudentNoticeSnapshot(
                ext == null || ext.getGradeYear() == null ? null : ext.getGradeYear() + "级",
                ext == null ? null : ext.getGradeYear(),
                ext == null ? null : ext.getMajorName(),
                ext != null && ext.getPartyStatus() != null && ext.getPartyStatus().contains("毕业")
        );
        Set<Long> directlyTargetedDeliveryIds = latestNoticeDeliveryTargetRepository.findByTargetUserId(studentId).stream()
                .map(LatestNoticeDeliveryTarget::getDeliveryId)
                .collect(Collectors.toSet());
        return latestNoticeItemRepository.findByIsDeletedOrderByPublishAtDesc(0).stream()
                .filter(item -> matchesStudent(item, snapshot, directlyTargetedDeliveryIds))
                .sorted(Comparator
                        .comparingInt((LatestNoticeItem item) -> scoreNotice(item, snapshot))
                        .reversed()
                        .thenComparing(item -> item.getPublishAt() == null ? LocalDateTime.MIN : item.getPublishAt(), Comparator.reverseOrder()))
                .map(item -> toResponse(item, snapshot))
                .toList();
    }

    private boolean matchesStudent(LatestNoticeItem item,
                                   StudentNoticeSnapshot snapshot,
                                   Set<Long> directlyTargetedDeliveryIds) {
        List<LatestNoticeDelivery> deliveries = latestNoticeDeliveryRepository.findByNoticeId(item.getId());
        if (deliveries.isEmpty()) {
            return true;
        }
        return deliveries.stream().anyMatch(delivery ->
                directlyTargetedDeliveryIds.contains(delivery.getId()) || matchesRule(delivery.getTargetRuleJson(), snapshot));
    }

    private boolean matchesRule(String targetRuleJson, StudentNoticeSnapshot snapshot) {
        if (targetRuleJson == null || targetRuleJson.isBlank()) {
            return true;
        }
        try {
            Map<String, Object> rule = objectMapper.readValue(targetRuleJson, new TypeReference<>() {
            });
            if (!matchesGradeYears(rule.get("gradeYears"), snapshot.gradeYear())) {
                return false;
            }
            if (!matchesMajors(rule.get("majors"), snapshot.major())) {
                return false;
            }
            if (!matchesGraduated(rule.get("graduatedOnly"), snapshot.graduated())) {
                return false;
            }
            return !Boolean.TRUE.equals(rule.get("scholarshipCandidates")) || hasScholarshipSignal(snapshot);
        } catch (Exception ex) {
            return true;
        }
    }

    private boolean matchesGradeYears(Object value, Integer gradeYear) {
        if (!(value instanceof List<?> values) || values.isEmpty()) {
            return true;
        }
        if (gradeYear == null) {
            return false;
        }
        return values.stream().map(String::valueOf).anyMatch(item -> item.equals(String.valueOf(gradeYear)));
    }

    private boolean matchesMajors(Object value, String major) {
        if (!(value instanceof List<?> values) || values.isEmpty()) {
            return true;
        }
        if (major == null || major.isBlank()) {
            return false;
        }
        return values.stream().map(String::valueOf).anyMatch(major::equals);
    }

    private boolean matchesGraduated(Object value, boolean graduated) {
        return !(value instanceof Boolean only) || !only || graduated;
    }

    private boolean hasScholarshipSignal(StudentNoticeSnapshot snapshot) {
        return snapshot.major() != null
                && (snapshot.major().contains("软件工程") || snapshot.major().contains("计算机科学"));
    }

    private TargetedNoticeResponse toResponse(LatestNoticeItem item, StudentNoticeSnapshot snapshot) {
        List<String> tags = resolveTags(item.getId());
        return new TargetedNoticeResponse(
                item.getId(),
                item.getTitle(),
                summarize(item),
                tags,
                buildTargetDescription(tags, snapshot),
                resolvePriority(item, snapshot),
                resolveMatchedRules(tags, snapshot),
                resolveDeliveryChannels(tags),
                item.getPublishAt() == null ? item.getCreatedAt() : item.getPublishAt()
        );
    }

    private List<String> resolveTags(Long noticeId) {
        return latestNoticeItemTagRepository.findByNoticeId(noticeId).stream()
                .map(LatestNoticeItemTag::getTagId)
                .map(id -> latestNoticeTagDictRepository.findById(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .filter(tag -> tag.getIsDeleted() != null && tag.getIsDeleted() == 0)
                .map(LatestNoticeTagDict::getTagName)
                .toList();
    }

    private String summarize(LatestNoticeItem item) {
        String content = item.getContent();
        if (content == null || content.isBlank()) {
            return item.getTitle();
        }
        return content.length() > 80 ? content.substring(0, 80) : content;
    }

    private int scoreNotice(LatestNoticeItem item, StudentNoticeSnapshot snapshot) {
        int score = 1;
        List<String> tags = resolveTags(item.getId());
        if (tags.stream().anyMatch(tag -> tag.contains("党") || tag.contains("团") || tag.contains("流程"))) {
            score += 3;
        }
        if (tags.stream().anyMatch(tag -> tag.contains("就业") || tag.contains("实习"))) {
            score += 2;
        }
        if (snapshot.major() != null && item.getContent() != null && item.getContent().contains(snapshot.major())) {
            score += 2;
        }
        if (snapshot.grade() != null && item.getContent() != null && item.getContent().contains(snapshot.grade())) {
            score += 2;
        }
        if (snapshot.graduated() && tags.stream().anyMatch(tag -> tag.contains("毕业"))) {
            score += 2;
        }
        return score;
    }

    private String buildTargetDescription(List<String> tags, StudentNoticeSnapshot snapshot) {
        if (snapshot.graduated() && tags.stream().anyMatch(tag -> tag.contains("毕业"))) {
            return "毕业生";
        }
        if (snapshot.grade() != null && snapshot.major() != null) {
            return snapshot.grade() + "/" + snapshot.major();
        }
        if (snapshot.grade() != null) {
            return snapshot.grade();
        }
        if (snapshot.major() != null) {
            return snapshot.major();
        }
        return "全体学生";
    }

    private String resolvePriority(LatestNoticeItem item, StudentNoticeSnapshot snapshot) {
        int score = scoreNotice(item, snapshot);
        if (score >= 6) {
            return "HIGH";
        }
        if (score >= 3) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> resolveMatchedRules(List<String> tags, StudentNoticeSnapshot snapshot) {
        java.util.ArrayList<String> rules = new java.util.ArrayList<>();
        if (snapshot.grade() != null) {
            rules.add("年级匹配");
        }
        if (snapshot.major() != null) {
            rules.add("专业匹配");
        }
        if (tags.stream().anyMatch(tag -> tag.contains("党") || tag.contains("团") || tag.contains("流程"))) {
            rules.add("党团事务标签");
        }
        if (tags.stream().anyMatch(tag -> tag.contains("就业") || tag.contains("实习"))) {
            rules.add("就业标签");
        }
        if (snapshot.graduated() && tags.stream().anyMatch(tag -> tag.contains("毕业"))) {
            rules.add("毕业生匹配");
        }
        if (rules.isEmpty()) {
            rules.add("全体学生");
        }
        return rules;
    }

    private List<String> resolveDeliveryChannels(List<String> tags) {
        if (tags.stream().anyMatch(tag -> tag.contains("就业") || tag.contains("实习"))) {
            return List.of("IN_APP", "EMAIL", "WECHAT");
        }
        if (tags.stream().anyMatch(tag -> tag.contains("党") || tag.contains("团") || tag.contains("流程"))) {
            return List.of("IN_APP", "EMAIL");
        }
        return List.of("IN_APP");
    }

    private record StudentNoticeSnapshot(String grade, Integer gradeYear, String major, boolean graduated) {
    }
}
