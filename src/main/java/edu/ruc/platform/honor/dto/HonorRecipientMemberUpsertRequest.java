package edu.ruc.platform.honor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HonorRecipientMemberUpsertRequest(
        Long studentId,
        @Size(max = 32, message = "学号不能超过 32 个字符") String studentNo,
        @NotBlank(message = "成员姓名不能为空") @Size(max = 64, message = "成员姓名不能超过 64 个字符") String studentName,
        @Size(max = 64, message = "专业不能超过 64 个字符") String major,
        @Size(max = 32, message = "年级不能超过 32 个字符") String grade,
        @Size(max = 32, message = "班级不能超过 32 个字符") String className,
        @Size(max = 64, message = "成员角色不能超过 64 个字符") String memberRole,
        Integer displayOrder
) {
}
