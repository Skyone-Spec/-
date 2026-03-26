package edu.ruc.platform.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentProfileUpsertRequest(
        @NotBlank(message = "学号不能为空") String studentNo,
        @NotBlank(message = "姓名不能为空") String name,
        String major,
        String grade,
        String className,
        String advisorScope,
        String degreeLevel,
        @Email(message = "邮箱格式不正确") String email,
        @NotNull(message = "毕业状态不能为空") Boolean graduated,
        String status,
        String majorChangedTo,
        String encryptedIdCardNo,
        String encryptedPhone,
        String encryptedNativePlace,
        String encryptedHouseholdAddress,
        String encryptedSupervisor
) {
}
