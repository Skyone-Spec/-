package edu.ruc.platform.student.support;

import java.util.Arrays;
import java.util.List;

public enum StudentGrowthModuleType {
    AWARD_SUPPORT(
            "award-support",
            "奖助情况",
            "DISABLED",
            "禁止修改",
            List.of(
                    new FieldDefinition("assessmentAcademicYear", "评定学年", true),
                    new FieldDefinition("awardName", "奖学金名称", true),
                    new FieldDefinition("batchName", "批次名称", false),
                    new FieldDefinition("awardLevel", "奖励级别", false),
                    new FieldDefinition("awardGrade", "奖励等级", false),
                    new FieldDefinition("awardAmount", "奖学金额（元）", false),
                    new FieldDefinition("awardType", "奖励类型", false)
            )
    ),
    COMPETITION(
            "competition",
            "学科竞赛",
            "SELF",
            "自主修改",
            List.of(
                    new FieldDefinition("awardDate", "获奖日期", true),
                    new FieldDefinition("competitionName", "获奖竞赛名称", true),
                    new FieldDefinition("competitionLevel", "获奖级别", false),
                    new FieldDefinition("competitionGrade", "获奖等级", false),
                    new FieldDefinition("competitionCategory", "获奖类别", false),
                    new FieldDefinition("organizer", "竞赛主办单位", false),
                    new FieldDefinition("advisorTeacherInfo", "指导教师信息", false),
                    new FieldDefinition("remarks", "其他说明", false)
            )
    ),
    INNOVATION_ENTREPRENEURSHIP(
            "innovation-entrepreneurship",
            "创新创业",
            "SELF",
            "自主修改",
            List.of(
                    new FieldDefinition("startDate", "开始日期", true),
                    new FieldDefinition("endDate", "结束日期", false),
                    new FieldDefinition("projectCode", "项目编号", false),
                    new FieldDefinition("projectName", "项目名称", true),
                    new FieldDefinition("collegeName", "项目依托学院", false),
                    new FieldDefinition("projectStatus", "项目状态", false),
                    new FieldDefinition("projectLevel", "项目级别", false),
                    new FieldDefinition("completionGrade", "结项等级", false),
                    new FieldDefinition("participantRole", "参与角色", false),
                    new FieldDefinition("projectType", "项目类型", false),
                    new FieldDefinition("projectBatch", "项目批次", false),
                    new FieldDefinition("participantCount", "参与学生总人数", false),
                    new FieldDefinition("advisorTeacher", "指导教师", false)
            )
    ),
    SOCIAL_PRACTICE(
            "social-practice",
            "社会实践",
            "SELF",
            "自主修改",
            List.of(
                    new FieldDefinition("practiceStartDate", "实践开始日期", true),
                    new FieldDefinition("practiceEndDate", "实践结束日期", false),
                    new FieldDefinition("practiceTeamName", "实践团名称", true),
                    new FieldDefinition("practiceTheme", "实践主题", true),
                    new FieldDefinition("practiceLocation", "实践地点", false),
                    new FieldDefinition("practiceTeamLevel", "实践团等级", false),
                    new FieldDefinition("advisorTeacher", "指导老师", false)
            )
    ),
    STUDENT_WORK(
            "student-work",
            "学生工作",
            "SELF",
            "自主修改",
            List.of(
                    new FieldDefinition("startDate", "开始日期", true),
                    new FieldDefinition("endDate", "结束日期", false),
                    new FieldDefinition("organizationName", "组织名称", true),
                    new FieldDefinition("positionName", "担任职务", true),
                    new FieldDefinition("workDescription", "工作情况", false)
            )
    ),
    VOLUNTEER_SERVICE(
            "volunteer-service",
            "志愿服务",
            "SELF",
            "自主修改",
            List.of(
                    new FieldDefinition("serviceDate", "志愿服务日期", true),
                    new FieldDefinition("serviceProject", "志愿服务项目", true),
                    new FieldDefinition("serviceLocation", "志愿服务地点", false),
                    new FieldDefinition("serviceDurationHours", "志愿服务时长", false),
                    new FieldDefinition("serviceOrganizationName", "志愿服务组织名称", false)
            )
    ),
    SKILL_CERTIFICATE(
            "skill-certificate",
            "技能证书",
            "SELF",
            "自主修改",
            List.of(
                    new FieldDefinition("certificateName", "技能/证书名称", true),
                    new FieldDefinition("obtainedDate", "获得时间", true),
                    new FieldDefinition("certificateLevel", "级别", false),
                    new FieldDefinition("description", "说明", false)
            )
    );

    private final String code;
    private final String moduleName;
    private final String editMode;
    private final String editModeLabel;
    private final List<FieldDefinition> fieldDefinitions;

    StudentGrowthModuleType(String code,
                            String moduleName,
                            String editMode,
                            String editModeLabel,
                            List<FieldDefinition> fieldDefinitions) {
        this.code = code;
        this.moduleName = moduleName;
        this.editMode = editMode;
        this.editModeLabel = editModeLabel;
        this.fieldDefinitions = fieldDefinitions;
    }

    public String code() {
        return code;
    }

    public String moduleName() {
        return moduleName;
    }

    public String editMode() {
        return editMode;
    }

    public String editModeLabel() {
        return editModeLabel;
    }

    public List<FieldDefinition> fieldDefinitions() {
        return fieldDefinitions;
    }

    public boolean editable() {
        return "SELF".equals(editMode);
    }

    public String labelOf(String key) {
        return fieldDefinitions.stream()
                .filter(item -> item.key().equals(key))
                .map(FieldDefinition::label)
                .findFirst()
                .orElse(key);
    }

    public static StudentGrowthModuleType fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的成长模块: " + code));
    }

    public record FieldDefinition(String key, String label, boolean required) {
    }
}
