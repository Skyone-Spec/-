package edu.ruc.platform.academic.service;

import edu.ruc.platform.academic.dto.AcademicAnalysisResponse;
import edu.ruc.platform.academic.dto.AcademicRiskSummaryResponse;
import edu.ruc.platform.academic.dto.AcademicWarningResponse;
import edu.ruc.platform.common.mock.MockDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockAcademicWarningService implements AcademicWarningApplicationService {

    private final MockDataStore mockDataStore;

    @Override
    public AcademicAnalysisResponse analyze(Long studentId) {
        List<String> recommendedCourses = mockDataStore.academicWarnings().stream()
                .map(AcademicWarningResponse::recommendedCourses)
                .filter(Objects::nonNull)
                .flatMap(item -> java.util.Arrays.stream(item.split("、")))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
        int totalRequiredCredits = mockDataStore.academicWarnings().stream()
                .mapToInt(item -> defaultZero(item.requiredCredits()))
                .sum();
        int totalEarnedCredits = mockDataStore.academicWarnings().stream()
                .mapToInt(item -> Math.min(defaultZero(item.earnedCredits()), defaultZero(item.requiredCredits())))
                .sum();
        int missingCredits = mockDataStore.academicWarnings().stream()
                .mapToInt(item -> defaultZero(item.missingCredits()))
                .sum();
        int completionRate = totalRequiredCredits == 0 ? 100 : (int) Math.round(totalEarnedCredits * 100.0 / totalRequiredCredits);
        String riskLevel = missingCredits >= 8 ? "HIGH" : "MEDIUM";
        String summary = missingCredits >= 8
                ? "存在较明显的课程模块缺口，建议优先处理核心课程补修与学分核验。"
                : "存在课程模块缺口，建议按模块优先级逐步补齐。";
        return new AcademicAnalysisResponse(
                studentId,
                "张三",
                "计算机类",
                "2023级",
                mockDataStore.academicWarnings(),
                recommendedCourses,
                totalRequiredCredits,
                totalEarnedCredits,
                missingCredits,
                completionRate,
                mockDataStore.academicWarnings().stream()
                        .filter(item -> defaultZero(item.missingCredits()) > 0)
                        .map(AcademicWarningResponse::moduleName)
                        .toList(),
                summary + "当前培养方案口径下整体完成率约为 " + completionRate
                        + "%，该结果仅作为辅助提醒，不直接用于毕业资格判断；补修、缓修、免修和课程替代仍需老师人工核验。",
                new AcademicRiskSummaryResponse(riskLevel, summary, true),
                "当前基于学生上报/基础规则生成，尚未直连完整教务数据。"
        );
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
