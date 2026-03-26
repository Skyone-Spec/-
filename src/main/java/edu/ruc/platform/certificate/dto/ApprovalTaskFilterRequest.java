package edu.ruc.platform.certificate.dto;

public record ApprovalTaskFilterRequest(
        Long studentId,
        String status,
        String certificateType,
        String keyword
) {
}
