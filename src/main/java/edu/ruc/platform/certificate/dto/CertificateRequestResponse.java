package edu.ruc.platform.certificate.dto;

public record CertificateRequestResponse(Long id, Long studentId, String certificateType, String status, String generatedPdfPath) {
}
