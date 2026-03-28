package edu.ruc.platform.academic.service;

import edu.ruc.platform.academic.domain.LatestAcademicAuditMissing;
import edu.ruc.platform.academic.domain.LatestAcademicAuditReport;
import edu.ruc.platform.academic.domain.LatestAcademicCourseRecommendation;
import edu.ruc.platform.academic.domain.LatestAcademicProgramModule;
import edu.ruc.platform.academic.domain.LatestAcademicTermCourse;
import edu.ruc.platform.academic.domain.LatestAcademicTranscript;
import edu.ruc.platform.academic.dto.AcademicAnalysisResponse;
import edu.ruc.platform.academic.dto.AcademicRiskSummaryResponse;
import edu.ruc.platform.academic.dto.AcademicWarningResponse;
import edu.ruc.platform.academic.repository.LatestAcademicAuditMissingRepository;
import edu.ruc.platform.academic.repository.LatestAcademicAuditReportRepository;
import edu.ruc.platform.academic.repository.LatestAcademicCourseRecommendationRepository;
import edu.ruc.platform.academic.repository.LatestAcademicProgramModuleRepository;
import edu.ruc.platform.academic.repository.LatestAcademicTermCourseRepository;
import edu.ruc.platform.academic.repository.LatestAcademicTranscriptRepository;
import edu.ruc.platform.auth.domain.LatestStudentExt;
import edu.ruc.platform.auth.domain.LatestUser;
import edu.ruc.platform.auth.repository.LatestStudentExtRepository;
import edu.ruc.platform.auth.repository.LatestUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Profile("kingbase")
@RequiredArgsConstructor
public class KingbaseAcademicWarningService implements AcademicWarningApplicationService {

    private final LatestAcademicAuditReportRepository latestAcademicAuditReportRepository;
    private final LatestAcademicAuditMissingRepository latestAcademicAuditMissingRepository;
    private final LatestAcademicCourseRecommendationRepository latestAcademicCourseRecommendationRepository;
    private final LatestAcademicProgramModuleRepository latestAcademicProgramModuleRepository;
    private final LatestAcademicTermCourseRepository latestAcademicTermCourseRepository;
    private final LatestAcademicTranscriptRepository latestAcademicTranscriptRepository;
    private final LatestUserRepository latestUserRepository;
    private final LatestStudentExtRepository latestStudentExtRepository;

    @Override
    public AcademicAnalysisResponse analyze(Long studentId) {
        LatestUser user = latestUserRepository.findById(studentId).orElse(null);
        LatestStudentExt ext = user == null || user.getStudentNo() == null
                ? null
                : latestStudentExtRepository.findByStudentNoAndIsDeleted(user.getStudentNo(), 0).orElse(null);
        LatestAcademicAuditReport report = latestAcademicAuditReportRepository
                .findFirstByStudentUserIdOrderByGeneratedAtDesc(studentId)
                .orElse(null);
        if (report == null) {
            return new AcademicAnalysisResponse(
                    studentId,
                    user == null ? "待补充" : user.getFullName(),
                    ext == null ? "待补充" : ext.getMajorName(),
                    ext == null || ext.getGradeYear() == null ? "待补充" : ext.getGradeYear() + "级",
                    List.of(),
                    List.of(),
                    0,
                    0,
                    0,
                    100,
                    List.of(),
                    "当前暂无学业预警。",
                    new AcademicRiskSummaryResponse("LOW", "当前未发现明显缺口。", false),
                    "当前结果直连 aca_audit_report / aca_audit_missing / aca_course_recommendation。"
            );
        }

        Map<Long, LatestAcademicProgramModule> modules = latestAcademicProgramModuleRepository
                .findByProgramIdAndIsDeleted(report.getProgramId(), 0).stream()
                .collect(Collectors.toMap(LatestAcademicProgramModule::getId, item -> item));
        Map<Long, List<LatestAcademicCourseRecommendation>> recommendationsByModule = latestAcademicCourseRecommendationRepository
                .findByReportId(report.getId()).stream()
                .collect(Collectors.groupingBy(LatestAcademicCourseRecommendation::getModuleId));
        Map<Long, LatestAcademicTermCourse> courses = latestAcademicTermCourseRepository.findAllById(
                latestAcademicCourseRecommendationRepository.findByReportId(report.getId()).stream()
                        .map(LatestAcademicCourseRecommendation::getCourseId)
                        .distinct()
                        .toList()
        ).stream().collect(Collectors.toMap(LatestAcademicTermCourse::getId, item -> item));

        List<AcademicWarningResponse> warnings = latestAcademicAuditMissingRepository.findByReportId(report.getId()).stream()
                .map(item -> toWarningResponse(item, modules.get(item.getModuleId()), recommendationsByModule.get(item.getModuleId()), courses))
                .filter(Objects::nonNull)
                .toList();

        LatestAcademicTranscript transcript = latestAcademicTranscriptRepository
                .findFirstByStudentUserIdOrderByParsedAtDesc(studentId)
                .orElse(null);
        int totalRequiredCredits = warnings.stream().mapToInt(item -> defaultZero(item.requiredCredits())).sum();
        int totalMissingCredits = warnings.stream().mapToInt(item -> defaultZero(item.missingCredits())).sum();
        int totalEarnedCredits = Math.max(totalRequiredCredits - totalMissingCredits,
                transcript == null || transcript.getTotalCredits() == null ? 0 : transcript.getTotalCredits().intValue());
        int completionRate = totalRequiredCredits == 0 ? 100 : Math.max(0, Math.min(100,
                (int) Math.round((totalRequiredCredits - totalMissingCredits) * 100.0 / totalRequiredCredits)));
        String riskLevel = warnings.isEmpty() ? "LOW" : (totalMissingCredits >= 8 ? "HIGH" : "MEDIUM");
        String riskMessage = warnings.isEmpty()
                ? "当前未发现明显缺口。"
                : (totalMissingCredits >= 8 ? "核心学分缺口较大，建议优先补修并人工复核。" : "存在课程模块缺口，建议优先补齐缺失学分。");

        return new AcademicAnalysisResponse(
                studentId,
                user == null ? "待补充" : user.getFullName(),
                ext == null ? "待补充" : ext.getMajorName(),
                ext == null || ext.getGradeYear() == null ? "待补充" : ext.getGradeYear() + "级",
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
                totalMissingCredits,
                completionRate,
                warnings.stream().map(AcademicWarningResponse::moduleName).toList(),
                warnings.isEmpty() ? "当前暂无学业预警。" : riskMessage + " 当前培养方案口径下整体完成率约为 " + completionRate + "%。",
                warnings.isEmpty()
                        ? new AcademicRiskSummaryResponse(riskLevel, riskMessage, false)
                        : new AcademicRiskSummaryResponse(riskLevel, riskMessage, true),
                "当前结果直连 aca_audit_report / aca_audit_missing / aca_course_recommendation。"
        );
    }

    private AcademicWarningResponse toWarningResponse(LatestAcademicAuditMissing missing,
                                                      LatestAcademicProgramModule module,
                                                      List<LatestAcademicCourseRecommendation> recommendations,
                                                      Map<Long, LatestAcademicTermCourse> courses) {
        if (module == null) {
            return null;
        }
        int requiredCredits = module.getRequiredCredits() == null ? 0 : module.getRequiredCredits().intValue();
        int missingCredits = missing.getMissingCredits() == null ? 0 : missing.getMissingCredits().intValue();
        int earnedCredits = Math.max(requiredCredits - missingCredits, 0);
        int completionRate = requiredCredits == 0 ? 100 : (int) Math.round(earnedCredits * 100.0 / requiredCredits);
        String recommendedCourses = recommendations == null ? null : recommendations.stream()
                .map(item -> courses.get(item.getCourseId()))
                .filter(Objects::nonNull)
                .map(LatestAcademicTermCourse::getCourseName)
                .distinct()
                .collect(Collectors.joining("、"));
        return new AcademicWarningResponse(
                module.getModuleName(),
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
