package edu.ruc.platform.certificate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CertificateTemplateCreateRequest(
        @NotBlank(message = "模板编码不能为空")
        @Size(max = 100, message = "模板编码长度不能超过100")
        String templateCode,
        @NotBlank(message = "模板名称不能为空")
        @Size(max = 200, message = "模板名称长度不能超过200")
        String templateName,
        @NotBlank(message = "证明类型不能为空")
        String certificateType,
        @Size(max = 2000, message = "模板内容长度不能超过2000")
        String templateContent,
        String templateFilePath,
        String outputFormat,
        String description
) {}
