package edu.ruc.platform.student.service;

import edu.ruc.platform.academic.dto.AcademicAnalysisResponse;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.common.enums.StudentActionPriority;
import edu.ruc.platform.common.enums.StudentActionType;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.platform.support.StudentActionPathRegistry;
import edu.ruc.platform.student.dto.StudentDashboardFocusItemResponse;
import edu.ruc.platform.student.dto.StudentGrowthSuggestionItemResponse;
import edu.ruc.platform.student.dto.StudentPortraitResponse;
import edu.ruc.platform.worklog.dto.StudentWorkloadSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class StudentActionRecommendationAssembler {

    public List<StudentDashboardFocusItemResponse> buildFocusItems(List<ReminderResponse> reminders,
                                                                   List<TargetedNoticeResponse> notices,
                                                                   List<CertificateRequestResponse> certificateRequests,
                                                                   AcademicAnalysisResponse academicAnalysis,
                                                                   StudentWorkloadSummaryResponse worklogSummary,
                                                                   StudentPortraitResponse portrait) {
        List<StudentDashboardFocusItemResponse> items = new java.util.ArrayList<>();
        reminders.stream().findFirst().ifPresent(reminder -> items.add(new StudentDashboardFocusItemResponse(
                StudentActionType.PARTY_REMINDER.name(),
                reminder.title(),
                reminder.content(),
                normalizePriority(reminder.level()),
                "查看党团提醒",
                StudentActionPathRegistry.resolve(StudentActionType.PARTY_REMINDER)
        )));
        certificateRequests.stream()
                .filter(item -> needsCertificateAttention(item.status()))
                .findFirst()
                .ifPresent(item -> items.add(buildCertificateFocusItem(item)));
        if (academicAnalysis.riskSummary() != null && !"LOW".equalsIgnoreCase(academicAnalysis.riskSummary().level())) {
            items.add(new StudentDashboardFocusItemResponse(
                    StudentActionType.ACADEMIC.name(),
                    "学业风险提示",
                    academicAnalysis.riskSummary().message(),
                    resolveAcademicPriority(academicAnalysis),
                    "查看学业分析",
                    StudentActionPathRegistry.resolve(StudentActionType.ACADEMIC)
            ));
        }
        if (shouldShowWorklogFocus(worklogSummary, portrait)) {
            items.add(buildWorklogFocusItem(worklogSummary, portrait));
        }
        if (shouldShowPortraitFocus(portrait)) {
            items.add(buildPortraitFocusItem(portrait));
        }
        notices.stream().findFirst().ifPresent(notice -> items.add(new StudentDashboardFocusItemResponse(
                StudentActionType.NOTICE.name(),
                notice.title(),
                notice.summary(),
                StudentActionPriority.MEDIUM.name(),
                "查看通知",
                StudentActionPathRegistry.resolve(StudentActionType.NOTICE)
        )));
        return items.stream()
                .sorted(Comparator.comparingInt(item -> priorityOrder(item.priority())))
                .toList();
    }

    public List<StudentGrowthSuggestionItemResponse> buildGrowthSuggestions(AcademicAnalysisResponse academicAnalysis,
                                                                            StudentWorkloadSummaryResponse worklogSummary,
                                                                            StudentPortraitResponse portrait,
                                                                            List<CertificateRequestResponse> certificateRequests) {
        List<StudentGrowthSuggestionItemResponse> suggestions = new java.util.ArrayList<>();
        if (academicAnalysis.riskSummary() != null && !"LOW".equalsIgnoreCase(academicAnalysis.riskSummary().level())) {
            suggestions.add(new StudentGrowthSuggestionItemResponse(
                    StudentActionType.ACADEMIC.name(),
                    "优先补齐学业短板",
                    academicAnalysis.riskSummary().message(),
                    resolveAcademicPriority(academicAnalysis),
                    "查看学业分析",
                    StudentActionPathRegistry.resolve(StudentActionType.ACADEMIC)
            ));
        } else if (portrait != null && portrait.gpa() != null && portrait.gpa() >= 3.6) {
            suggestions.add(new StudentGrowthSuggestionItemResponse(
                    StudentActionType.ACADEMIC.name(),
                    "保持学业优势",
                    "当前 GPA 为 " + portrait.gpa() + "，建议继续保持核心课程成绩，并同步准备评优或升学材料。",
                    StudentActionPriority.LOW.name(),
                    "查看学生画像",
                    StudentActionPathRegistry.resolve(StudentActionType.PORTRAIT)
            ));
        }
        if (worklogSummary.totalEntries() == 0 || worklogSummary.totalWorkloadScore() < 3) {
            suggestions.add(new StudentGrowthSuggestionItemResponse(
                    StudentActionType.WORKLOG.name(),
                    "尽快补充过程性记录",
                    "当前工作记录较少，建议优先补录班级事务、党团活动或志愿服务经历，便于后续证明与画像使用。",
                    StudentActionPriority.MEDIUM.name(),
                    "查看工作记录",
                    StudentActionPathRegistry.resolve(StudentActionType.WORKLOG)
            ));
        } else if (worklogSummary.totalWorkloadScore() >= 5) {
            suggestions.add(new StudentGrowthSuggestionItemResponse(
                    StudentActionType.WORKLOG.name(),
                    "沉淀已有成果材料",
                    "已累计登记 " + worklogSummary.totalWorkloadScore() + " 分工作量，建议同步整理证明、总结和展示材料。",
                    StudentActionPriority.MEDIUM.name(),
                    "查看工作记录",
                    StudentActionPathRegistry.resolve(StudentActionType.WORKLOG)
            ));
        }
        if (portrait == null || portrait.careerOrientation() == null || portrait.careerOrientation().isBlank()) {
            suggestions.add(new StudentGrowthSuggestionItemResponse(
                    StudentActionType.PORTRAIT.name(),
                    "补全成长画像方向",
                    "当前职业倾向尚未明确，建议补充画像信息，便于后续个性化推荐与成果展示。",
                    StudentActionPriority.MEDIUM.name(),
                    "查看学生画像",
                    StudentActionPathRegistry.resolve(StudentActionType.PORTRAIT)
            ));
        } else {
            suggestions.add(new StudentGrowthSuggestionItemResponse(
                    StudentActionType.PORTRAIT.name(),
                    "围绕目标方向完善经历",
                    "当前职业倾向为" + portrait.careerOrientation() + "，建议围绕该方向持续补充实践、竞赛或研究经历。",
                    StudentActionPriority.LOW.name(),
                    "查看学生画像",
                    StudentActionPathRegistry.resolve(StudentActionType.PORTRAIT)
            ));
        }
        certificateRequests.stream()
                .filter(item -> needsCertificateAttention(item.status()))
                .findFirst()
                .ifPresent(item -> suggestions.add(new StudentGrowthSuggestionItemResponse(
                        StudentActionType.CERTIFICATE.name(),
                        "及时处理证明申请",
                        "当前有" + item.certificateType() + "处于" + item.status() + "状态，建议跟进审批或重新提交，避免影响使用。",
                        StudentActionPriority.HIGH.name(),
                        "查看证明申请",
                        StudentActionPathRegistry.resolve(StudentActionType.CERTIFICATE)
                )));
        return suggestions.stream()
                .sorted(Comparator.comparingInt(item -> priorityOrder(item.priority())))
                .toList();
    }

    private StudentDashboardFocusItemResponse buildCertificateFocusItem(CertificateRequestResponse item) {
        if ("WITHDRAWN".equalsIgnoreCase(item.status())) {
            return new StudentDashboardFocusItemResponse(
                    StudentActionType.CERTIFICATE.name(),
                    item.certificateType() + "待重新提交",
                    "当前申请已撤回，若仍需使用，请尽快补充材料后重新提交。",
                    StudentActionPriority.HIGH.name(),
                    "重新发起申请",
                    StudentActionPathRegistry.resolve(StudentActionType.CERTIFICATE)
            );
        }
        return new StudentDashboardFocusItemResponse(
                StudentActionType.CERTIFICATE.name(),
                item.certificateType() + "申请处理中",
                "当前状态：" + item.status() + "，建议及时关注审批进度并准备后续下载使用。",
                StudentActionPriority.HIGH.name(),
                "查看证明申请",
                StudentActionPathRegistry.resolve(StudentActionType.CERTIFICATE)
        );
    }

    private String resolveAcademicPriority(AcademicAnalysisResponse academicAnalysis) {
        if (academicAnalysis.riskSummary() == null) {
            return StudentActionPriority.LOW.name();
        }
        if ("HIGH".equalsIgnoreCase(academicAnalysis.riskSummary().level())
                || academicAnalysis.riskSummary().needsManualReview()) {
            return StudentActionPriority.HIGH.name();
        }
        return StudentActionPriority.MEDIUM.name();
    }

    private boolean needsCertificateAttention(String status) {
        return "PENDING".equalsIgnoreCase(status) || "WITHDRAWN".equalsIgnoreCase(status);
    }

    private boolean shouldShowWorklogFocus(StudentWorkloadSummaryResponse worklogSummary,
                                           StudentPortraitResponse portrait) {
        return worklogSummary.totalEntries() == 0
                || worklogSummary.totalWorkloadScore() < 3
                || worklogSummary.totalWorkloadScore() >= 5
                || isCompetitivePortrait(portrait);
    }

    private StudentDashboardFocusItemResponse buildWorklogFocusItem(StudentWorkloadSummaryResponse worklogSummary,
                                                                    StudentPortraitResponse portrait) {
        if (worklogSummary.totalEntries() == 0) {
            return new StudentDashboardFocusItemResponse(
                    StudentActionType.WORKLOG.name(),
                    "近期缺少学生工作记录",
                    "当前尚未登记任何工作记录，建议及时补录活动、事务或服务经历，避免材料沉淀缺失。",
                    StudentActionPriority.MEDIUM.name(),
                    "去登记工作记录",
                    StudentActionPathRegistry.resolve(StudentActionType.WORKLOG)
            );
        }
        if (worklogSummary.totalWorkloadScore() < 3) {
            return new StudentDashboardFocusItemResponse(
                    StudentActionType.WORKLOG.name(),
                    "学生工作记录偏少",
                    "当前仅登记 " + worklogSummary.totalEntries() + " 条、累计 " + worklogSummary.totalWorkloadScore()
                            + " 分，建议继续补充班级、党团或志愿服务经历。",
                    StudentActionPriority.MEDIUM.name(),
                    "查看工作记录",
                    StudentActionPathRegistry.resolve(StudentActionType.WORKLOG)
            );
        }
        if (isCompetitivePortrait(portrait)) {
            return new StudentDashboardFocusItemResponse(
                    StudentActionType.WORKLOG.name(),
                    "优势经历建议尽快沉淀",
                    "画像显示你具备较强竞争力，已登记 " + worklogSummary.totalWorkloadScore()
                            + " 分工作量，建议同步完善成果证明与展示材料。",
                    StudentActionPriority.MEDIUM.name(),
                    "查看工作记录",
                    StudentActionPathRegistry.resolve(StudentActionType.WORKLOG)
            );
        }
        return new StudentDashboardFocusItemResponse(
                StudentActionType.WORKLOG.name(),
                "近期学生工作较活跃",
                "已累计登记 " + worklogSummary.totalWorkloadScore() + " 分工作量，可及时沉淀成果材料。",
                StudentActionPriority.MEDIUM.name(),
                "查看工作记录",
                StudentActionPathRegistry.resolve(StudentActionType.WORKLOG)
        );
    }

    private boolean shouldShowPortraitFocus(StudentPortraitResponse portrait) {
        return portrait != null
                && ((portrait.careerOrientation() != null && !portrait.careerOrientation().isBlank())
                || portrait.gpa() != null
                || portrait.gradeRank() != null
                || portrait.honors() != null && !portrait.honors().isBlank());
    }

    private StudentDashboardFocusItemResponse buildPortraitFocusItem(StudentPortraitResponse portrait) {
        if (portrait.gpa() != null && portrait.gpa() < 3.0) {
            return new StudentDashboardFocusItemResponse(
                    StudentActionType.PORTRAIT.name(),
                    "画像提示需补强学业表现",
                    "当前 GPA 为 " + portrait.gpa() + "，建议优先提升核心课程成绩，并同步补充成长记录。",
                    StudentActionPriority.HIGH.name(),
                    "查看学生画像",
                    StudentActionPathRegistry.resolve(StudentActionType.PORTRAIT)
            );
        }
        if (portrait.gradeRank() != null && portrait.gradeRank() <= 20) {
            return new StudentDashboardFocusItemResponse(
                    StudentActionType.PORTRAIT.name(),
                    "画像优势明显",
                    buildPortraitStrengthDescription(portrait),
                    StudentActionPriority.LOW.name(),
                    "完善展示材料",
                    StudentActionPathRegistry.resolve(StudentActionType.PORTRAIT)
            );
        }
        if (portrait.careerOrientation() != null && !portrait.careerOrientation().isBlank()) {
            return new StudentDashboardFocusItemResponse(
                    StudentActionType.PORTRAIT.name(),
                    "成长画像提示",
                    "当前职业倾向为" + portrait.careerOrientation() + "，建议结合画像继续完善经历材料。",
                    StudentActionPriority.LOW.name(),
                    "查看学生画像",
                    StudentActionPathRegistry.resolve(StudentActionType.PORTRAIT)
            );
        }
        return new StudentDashboardFocusItemResponse(
                StudentActionType.PORTRAIT.name(),
                "完善成长画像",
                "建议补充荣誉、实践和学业表现信息，便于后续推荐与成果展示。",
                StudentActionPriority.LOW.name(),
                "查看学生画像",
                StudentActionPathRegistry.resolve(StudentActionType.PORTRAIT)
        );
    }

    private String buildPortraitStrengthDescription(StudentPortraitResponse portrait) {
        String honors = portrait.honors() != null && !portrait.honors().isBlank() ? "，已有" + portrait.honors() : "";
        String orientation = portrait.careerOrientation() != null && !portrait.careerOrientation().isBlank()
                ? "，职业倾向偏向" + portrait.careerOrientation() : "";
        return "当前年级排名靠前" + honors + orientation + "，建议及时整理成果用于评优、推荐或升学材料。";
    }

    private boolean isCompetitivePortrait(StudentPortraitResponse portrait) {
        return portrait != null
                && ((portrait.gpa() != null && portrait.gpa() >= 3.6)
                || (portrait.gradeRank() != null && portrait.gradeRank() <= 20)
                || (portrait.honors() != null && !portrait.honors().isBlank()));
    }

    private String normalizePriority(String level) {
        return switch (level) {
            case "HIGH" -> StudentActionPriority.HIGH.name();
            case "MEDIUM" -> StudentActionPriority.MEDIUM.name();
            default -> StudentActionPriority.LOW.name();
        };
    }

    private int priorityOrder(String priority) {
        return switch (StudentActionPriority.valueOf(priority)) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            default -> 2;
        };
    }
}
