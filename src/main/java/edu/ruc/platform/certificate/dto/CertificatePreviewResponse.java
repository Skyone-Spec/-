package edu.ruc.platform.certificate.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record CertificatePreviewResponse(
        Long requestId,
        Long studentId,
        String studentName,
        String certificateType,
        String status,
        String currentApproverRole,
        Integer approvalLevel,
        String reason,
        String previewUrl,
        String outputFormat,
        Map<String, String> templateFields,
        String generatedContent,
        String nextStepHint,
        LocalDateTime submittedAt,
        LocalDateTime withdrawalDeadline,
        boolean canWithdraw,
        boolean canResubmit
) {
}
