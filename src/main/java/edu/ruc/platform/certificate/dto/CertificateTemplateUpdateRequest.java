package edu.ruc.platform.certificate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CertificateTemplateUpdateRequest(
        @Size(max = 200, message = "模板名称长度不能超过200")
        String templateName,
        String certificateType,
        @Size(max = 2000, message = "模板内容长度不能超过2000")
        String templateContent,
        String templateFilePath,
        String outputFormat,
        Boolean isActive,
        String description
) {}
