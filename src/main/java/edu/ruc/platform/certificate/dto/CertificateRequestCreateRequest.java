package edu.ruc.platform.certificate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CertificateRequestCreateRequest(
        @NotNull(message = "学生ID不能为空") Long studentId,
        @NotBlank(message = "证明类型不能为空") String certificateType,
        String reason
) {
}
