package edu.ruc.platform.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlatformImportErrorCreateRequest(
        @NotNull(message = "错误行号不能为空") @Min(value = 1, message = "错误行号必须大于 0") Integer rowNumber,
        @NotBlank(message = "错误字段不能为空") String fieldName,
        @NotBlank(message = "错误信息不能为空") String errorMessage,
        String rawValue
) {
}
