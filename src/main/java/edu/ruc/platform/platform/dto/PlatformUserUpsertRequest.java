package edu.ruc.platform.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record PlatformUserUpsertRequest(
        @NotBlank(message = "用户名不能为空") String username,
        @NotBlank(message = "角色不能为空") String role,
        Boolean enabled,
        String rawPassword,
        Boolean passwordResetRequired
) {
}
