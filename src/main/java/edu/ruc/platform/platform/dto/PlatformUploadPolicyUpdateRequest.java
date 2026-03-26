package edu.ruc.platform.platform.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlatformUploadPolicyUpdateRequest(
        @Positive(message = "上传大小限制必须大于 0")
        Long maxFileSizeBytes,
        @Size(min = 1, message = "允许的文件类型不能为空")
        List<String> allowedContentTypes,
        Boolean allowEmptyContentType
) {
}
