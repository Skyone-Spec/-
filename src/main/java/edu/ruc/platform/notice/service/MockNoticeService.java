package edu.ruc.platform.notice.service;

import edu.ruc.platform.common.mock.MockDataStore;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.auth.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockNoticeService implements NoticeApplicationService {

    private final MockDataStore mockDataStore;

    @Override
    public List<TargetedNoticeResponse> listTargetedNotices(Long studentId) {
        UserProfileResponse user = switch (studentId.intValue()) {
            case 10001 -> mockDataStore.user("2023100001");
            case 10002 -> mockDataStore.user("2023100002");
            default -> null;
        };
        if (user == null) {
            throw new BusinessException("未找到学生信息");
        }
        return mockDataStore.notices().stream()
                .filter(notice -> !matchedRules(notice, user).isEmpty())
                .sorted(Comparator
                        .comparingInt((TargetedNoticeResponse notice) -> scoreNotice(notice, user))
                        .reversed()
                        .thenComparing(TargetedNoticeResponse::publishTime, Comparator.reverseOrder()))
                .map(notice -> new TargetedNoticeResponse(
                        notice.id(),
                        notice.title(),
                        notice.summary(),
                        notice.tags(),
                        notice.targetDescription(),
                        resolvePriority(notice, user),
                        matchedRules(notice, user),
                        resolveDeliveryChannels(notice),
                        notice.publishTime()))
                .toList();
    }

    private int scoreNotice(TargetedNoticeResponse notice, UserProfileResponse user) {
        int score = 0;
        if ("全体学生".equals(notice.targetDescription())) {
            score += 1;
        }
        if (user.grade() != null && notice.targetDescription().contains(user.grade())) {
            score += 3;
        }
        if (user.major() != null && notice.targetDescription().contains(user.major())) {
            score += 3;
        }
        if (notice.tags().stream().anyMatch(tag -> "流程".equals(tag) || "党团事务".equals(tag))) {
            score += 2;
        }
        if (notice.tags().stream().anyMatch(tag -> "就业".equals(tag) || "实习".equals(tag))) {
            score += 2;
        }
        return score;
    }

    private List<String> matchedRules(TargetedNoticeResponse notice, UserProfileResponse user) {
        List<String> rules = new ArrayList<>();
        if ("全体学生".equals(notice.targetDescription())) {
            rules.add("全体学生");
        }
        if (user.grade() != null && notice.targetDescription().contains(user.grade())) {
            rules.add("年级匹配");
        }
        if (user.major() != null && notice.targetDescription().contains(user.major())) {
            rules.add("专业匹配");
        }
        if (notice.tags().stream().anyMatch(tag -> "就业".equals(tag) || "实习".equals(tag))) {
            rules.add("就业标签");
        }
        if (notice.tags().stream().anyMatch(tag -> "流程".equals(tag) || "党团事务".equals(tag))) {
            rules.add("党团事务标签");
        }
        return rules;
    }

    private String resolvePriority(TargetedNoticeResponse notice, UserProfileResponse user) {
        int score = scoreNotice(notice, user);
        if (score >= 6) {
            return "HIGH";
        }
        if (score >= 3) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> resolveDeliveryChannels(TargetedNoticeResponse notice) {
        if (notice.tags().stream().anyMatch(tag -> "就业".equals(tag) || "实习".equals(tag))) {
            return List.of("IN_APP", "EMAIL", "WECHAT");
        }
        if (notice.tags().stream().anyMatch(tag -> "流程".equals(tag) || "党团事务".equals(tag))) {
            return List.of("IN_APP", "EMAIL");
        }
        return List.of("IN_APP");
    }
}
