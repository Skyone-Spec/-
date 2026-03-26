package edu.ruc.platform.certificate.dto;

import jakarta.validation.constraints.NotBlank;

public record CertificateRequestActionRequest(
        @NotBlank(message = "操作不能为空") String action,
        String comment
) {
}
