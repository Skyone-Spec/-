package edu.ruc.platform.certificate.service;

import edu.ruc.platform.certificate.dto.*;
import java.util.List;

public interface CertificateTemplateApplicationService {
    List<CertificateTemplateResponse> listAll();
    List<CertificateTemplateResponse> listActive();
    List<CertificateTemplateResponse> listByType(String certificateType);
    CertificateTemplateResponse getById(Long id);
    CertificateTemplateResponse getByCode(String templateCode);
    CertificateTemplateResponse create(CertificateTemplateCreateRequest request);
    CertificateTemplateResponse update(Long id, CertificateTemplateUpdateRequest request);
    void delete(Long id);
    String renderPreview(Long templateId, Long studentId);
}
