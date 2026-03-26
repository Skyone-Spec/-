package edu.ruc.platform.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PlatformSecurityPolicyUpdateRequest(
        @Min(value = 1, message = "最大失败次数必须大于 0")
        Integer maxFailedLoginAttempts,
        @Min(value = 1, message = "锁定时长必须大于 0")
        Integer lockDurationMinutes,
        @Size(min = 6, max = 64, message = "默认密码长度必须在 6 到 64 位之间")
        String defaultPassword,
        Boolean requirePasswordResetOnCreate
) {
}
