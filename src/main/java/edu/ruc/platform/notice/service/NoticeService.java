package edu.ruc.platform.notice.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.notice.domain.Notice;
import edu.ruc.platform.notice.dto.NoticeResponse;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.notice.repository.NoticeRepository;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Profile("!mock & !kingbase")
@RequiredArgsConstructor
public class NoticeService implements NoticeApplicationService {

    private final NoticeRepository noticeRepository;
    private final StudentProfileRepository studentProfileRepository;

    public List<NoticeResponse> listAll() {
        return noticeRepository.findAllByOrderByPublishTimeDesc()
                .stream()
                .map(notice -> new NoticeResponse(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getSummary(),
                        notice.getTag(),
                        notice.getPublishTime()
                ))
                .toList();
    }

    @Override
    public List<TargetedNoticeResponse> listTargetedNotices(Long studentId) {
        StudentProfile studentProfile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("未找到学生信息"));
        return noticeRepository.findAllByOrderByPublishTimeDesc()
                .stream()
                .filter(notice -> matchesStudent(notice, studentProfile))
                .sorted(Comparator
                        .comparingInt((Notice notice) -> scoreNotice(notice, studentProfile))
                        .reversed()
                        .thenComparing(Notice::getPublishTime, Comparator.reverseOrder()))
                .map(notice -> new TargetedNoticeResponse(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getSummary(),
                        resolveTags(notice),
                        buildTargetDescription(notice),
                        resolvePriority(notice, studentProfile),
                        resolveMatchedRules(notice, studentProfile),
                        resolveDeliveryChannels(notice),
                        notice.getPublishTime()
                ))
                .toList();
    }

    private boolean matchesStudent(Notice notice, StudentProfile studentProfile) {
        boolean gradeMatched = notice.getTargetGrade() == null || notice.getTargetGrade().isBlank()
                || notice.getTargetGrade().equals(studentProfile.getGrade());
        boolean majorMatched = notice.getTargetMajor() == null || notice.getTargetMajor().isBlank()
                || notice.getTargetMajor().equals(studentProfile.getMajor());
        boolean graduateMatched = !Boolean.TRUE.equals(notice.getTargetGraduateOnly())
                || Boolean.TRUE.equals(studentProfile.getGraduated());
        return gradeMatched && majorMatched && graduateMatched;
    }

    private int scoreNotice(Notice notice, StudentProfile studentProfile) {
        int score = 0;
        if ((notice.getTargetGrade() == null || notice.getTargetGrade().isBlank())
                && (notice.getTargetMajor() == null || notice.getTargetMajor().isBlank())
                && !Boolean.TRUE.equals(notice.getTargetGraduateOnly())) {
            score += 1;
        }
        if (studentProfile.getGrade() != null && studentProfile.getGrade().equals(notice.getTargetGrade())) {
            score += 3;
        }
        if (studentProfile.getMajor() != null && studentProfile.getMajor().equals(notice.getTargetMajor())) {
            score += 3;
        }
        if (notice.getTag() != null && (notice.getTag().contains("流程") || notice.getTag().contains("党团事务"))) {
            score += 2;
        }
        if (notice.getTag() != null && (notice.getTag().contains("就业") || notice.getTag().contains("实习"))) {
            score += 2;
        }
        if (Boolean.TRUE.equals(notice.getTargetGraduateOnly()) && Boolean.TRUE.equals(studentProfile.getGraduated())) {
            score += 2;
        }
        return score;
    }

    private String buildTargetDescription(Notice notice) {
        if (notice.getTargetGrade() != null && notice.getTargetMajor() != null) {
            return notice.getTargetGrade() + "/" + notice.getTargetMajor();
        }
        if (notice.getTargetGrade() != null) {
            return notice.getTargetGrade();
        }
        if (notice.getTargetMajor() != null) {
            return notice.getTargetMajor();
        }
        return Boolean.TRUE.equals(notice.getTargetGraduateOnly()) ? "毕业生" : "全体学生";
    }

    private String resolvePriority(Notice notice, StudentProfile studentProfile) {
        int score = scoreNotice(notice, studentProfile);
        if (score >= 6) {
            return "HIGH";
        }
        if (score >= 3) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> resolveMatchedRules(Notice notice, StudentProfile studentProfile) {
        List<String> rules = new java.util.ArrayList<>();
        if ((notice.getTargetGrade() == null || notice.getTargetGrade().isBlank())
                && (notice.getTargetMajor() == null || notice.getTargetMajor().isBlank())
                && !Boolean.TRUE.equals(notice.getTargetGraduateOnly())) {
            rules.add("全体学生");
        }
        if (studentProfile.getGrade() != null && studentProfile.getGrade().equals(notice.getTargetGrade())) {
            rules.add("年级匹配");
        }
        if (studentProfile.getMajor() != null && studentProfile.getMajor().equals(notice.getTargetMajor())) {
            rules.add("专业匹配");
        }
        if (notice.getTag() != null && (notice.getTag().contains("就业") || notice.getTag().contains("实习"))) {
            rules.add("就业标签");
        }
        if (notice.getTag() != null && (notice.getTag().contains("流程") || notice.getTag().contains("党团事务"))) {
            rules.add("党团事务标签");
        }
        if (Boolean.TRUE.equals(notice.getTargetGraduateOnly()) && Boolean.TRUE.equals(studentProfile.getGraduated())) {
            rules.add("毕业生匹配");
        }
        return rules;
    }

    private List<String> resolveDeliveryChannels(Notice notice) {
        if (notice.getTag() != null && (notice.getTag().contains("就业") || notice.getTag().contains("实习"))) {
            return List.of("IN_APP", "EMAIL", "WECHAT");
        }
        if (notice.getTag() != null && (notice.getTag().contains("流程") || notice.getTag().contains("党团事务"))) {
            return List.of("IN_APP", "EMAIL");
        }
        return List.of("IN_APP");
    }

    private List<String> resolveTags(Notice notice) {
        if (notice.getTag() == null || notice.getTag().isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(notice.getTag().split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }
}
