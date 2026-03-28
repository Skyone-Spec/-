package edu.ruc.platform.student.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record StudentPortraitUpsertRequest(
        @Size(max = 32, message = "性别长度不能超过 32") String gender,
        @Size(max = 64, message = "民族长度不能超过 64") String ethnicity,
        @Size(max = 500, message = "荣誉信息长度不能超过 500") String honors,
        @Size(max = 500, message = "奖学金信息长度不能超过 500") String scholarships,
        @Size(max = 500, message = "竞赛信息长度不能超过 500") String competitions,
        @Size(max = 500, message = "社会实践信息长度不能超过 500") String socialPractice,
        @Size(max = 500, message = "志愿服务信息长度不能超过 500") String volunteerService,
        @Size(max = 500, message = "科研经历长度不能超过 500") String researchExperience,
        @Size(max = 500, message = "违纪记录长度不能超过 500") String disciplineRecords,
        @Size(max = 500, message = "日常表现长度不能超过 500") String dailyPerformance,
        @DecimalMin(value = "0.0", message = "学生画像 GPA 必须在 0 到 4.5 之间")
        @DecimalMax(value = "4.5", message = "学生画像 GPA 必须在 0 到 4.5 之间")
        Double gpa,
        @Positive(message = "年级排名必须大于 0") Integer gradeRank,
        @Positive(message = "专业排名必须大于 0") Integer majorRank,
        @PositiveOrZero(message = "已修学分不能小于 0") Integer creditsEarned,
        @Size(max = 128, message = "职业方向长度不能超过 128") String careerOrientation,
        @Size(max = 1000, message = "备注长度不能超过 1000") String remarks,
        @Size(max = 64, message = "维护人长度不能超过 64") String updatedBy,
        @Size(max = 64, message = "数据来源长度不能超过 64") String dataSource,
        Boolean publicVisible
) {
}
