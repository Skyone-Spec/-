package edu.ruc.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record WechatLoginRequest(
        @NotBlank(message = "微信code不能为空") String code
) {
}
