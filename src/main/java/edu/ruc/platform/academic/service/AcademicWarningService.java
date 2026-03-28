package edu.ruc.platform.academic.service;

import edu.ruc.platform.academic.dto.AcademicAnalysisResponse;
import edu.ruc.platform.academic.dto.AcademicRiskSummaryResponse;
import edu.ruc.platform.academic.dto.AcademicWarningResponse;
import edu.ruc.platform.academic.repository.AcademicWarningRecordRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Profile("!mock & !kingbase")
@RequiredArgsConstructor
public class AcademicWarningService implements AcademicWarningApplicationService {

    private final AcademicWarningRecordRepository academicWarningRecordRepository;
    private final StudentProfileRepository studentProfileRepository;

    public List<AcademicWarningResponse> listByStudentId(Long studentId) {
        return academicWarningRecordRepository.findByStudentId(studentId)
                .stream()
                .map(record -> toWarningResponse(record.getModuleName(),
                        record.getRequiredCredits(),
                        record.getEarnedCredits(),
                        record.getRecommendedCourses()))
                .toList();
    }

    @Override
    public AcademicAnalysisResponse analyze(Long studentId) {
        List<AcademicWarningResponse> warnings = listByStudentId(studentId);
        var student = studentProfileRepository.findById(studentId).orElse(null);
        int totalRequiredCredits = warnings.stream()
                .mapToInt(item -> defaultZero(item.requiredCredits()))
                .sum();
        int totalEarnedCredits = warnings.stream()
                .mapToInt(item -> Math.min(defaultZero(item.earnedCredits()), defaultZero(item.requiredCredits())))
                .sum();
        int missingCredits = warnings.stream()
                .mapToInt(item -> defaultZero(item.missingCredits()))
                .sum();
        int completionRate = totalRequiredCredits == 0 ? 100 : (int) Math.round(totalEarnedCredits * 100.0 / totalRequiredCredits);
        String riskLevel = warnings.isEmpty() ? "LOW" : (missingCredits >= 8 ? "HIGH" : "MEDIUM");
        String riskMessage = warnings.isEmpty()
                ? "当前未发现明显缺口。"
                : (missingCredits >= 8 ? "核心学分缺口较大，建议优先补修并人工复核。" : "存在课程模块缺口，建议优先补齐缺失学分。");
        return new AcademicAnalysisResponse(
                studentId,
                student == null ? "待补充" : student.getName(),
                student == null ? "待补充" : student.getMajor(),
                student == null ? "待补充" : student.getGrade(),
                warnings,
                warnings.stream()
                        .map(AcademicWarningResponse::recommendedCourses)
                        .filter(Objects::nonNull)
                        .flatMap(item -> java.util.Arrays.stream(item.split("、")))
                        .map(String::trim)
                        .filter(item -> !item.isBlank())
                        .distinct()
                        .toList(),
                totalRequiredCredits,
                totalEarnedCredits,
                missingCredits,
                completionRate,
                warnings.stream()
                        .filter(item -> defaultZero(item.missingCredits()) > 0)
                        .map(AcademicWarningResponse::moduleName)
                        .toList(),
                warnings.isEmpty() ? "当前暂无学业预警。" : riskMessage + " 当前培养方案口径下整体完成率约为 " + completionRate + "%。",
                warnings.isEmpty()
                        ? new AcademicRiskSummaryResponse(riskLevel, riskMessage, false)
                        : new AcademicRiskSummaryResponse(riskLevel, riskMessage, true),
                "当前未直连完整教务系统，复杂规则场景需人工确认。"
        );
    }

    private AcademicWarningResponse toWarningResponse(String moduleName,
                                                     Integer requiredCredits,
                                                     Integer earnedCredits,
                                                     String recommendedCourses) {
        int safeRequiredCredits = defaultZero(requiredCredits);
        int safeEarnedCredits = Math.min(defaultZero(earnedCredits), safeRequiredCredits);
        int missingCredits = Math.max(safeRequiredCredits - safeEarnedCredits, 0);
        int completionRate = safeRequiredCredits == 0 ? 100 : (int) Math.round(safeEarnedCredits * 100.0 / safeRequiredCredits);
        return new AcademicWarningResponse(
                moduleName,
                requiredCredits,
                earnedCredits,
                missingCredits,
                completionRate,
                recommendedCourses
        );
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }
}
