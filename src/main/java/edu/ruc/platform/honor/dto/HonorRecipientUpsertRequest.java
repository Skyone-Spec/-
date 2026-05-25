package edu.ruc.platform.honor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record HonorRecipientUpsertRequest(
        @NotBlank(message = "获得者类型不能为空") @Size(max = 32, message = "获得者类型不能超过 32 个字符") String recipientType,
        Long studentId,
        @Size(max = 32, message = "学号不能超过 32 个字符") String studentNo,
        @NotBlank(message = "获得者名称不能为空") @Size(max = 128, message = "获得者名称不能超过 128 个字符") String recipientName,
        @Size(max = 64, message = "专业不能超过 64 个字符") String major,
        @Size(max = 32, message = "年级不能超过 32 个字符") String grade,
        @Size(max = 32, message = "班级不能超过 32 个字符") String className,
        String awardIntro,
        String advancedDeeds,
        Long photoFileId,
        Boolean publicVisible,
        Integer displayOrder,
        LocalDateTime displayStartAt,
        LocalDateTime displayEndAt,
        Long importTaskId
) {
}
