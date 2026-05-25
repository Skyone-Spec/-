package edu.ruc.platform.academic.service;

import edu.ruc.platform.academic.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AcademicProgramApplicationService {
    List<AcademicProgramResponse> listAll();
    AcademicProgramResponse getById(Long id);
    AcademicProgramResponse create(AcademicProgramCreateRequest request);
    AcademicProgramResponse update(Long id, AcademicProgramCreateRequest request);
    void delete(Long id);
    AcademicProgramModuleResponse addModule(Long programId, AcademicProgramModuleCreateRequest request);
    void removeModule(Long moduleId);
    TranscriptUploadResponse uploadTranscript(Long studentId, MultipartFile file);
    TranscriptUploadResponse getTranscript(Long studentId);
    AuditReportResponse generateAuditReport(Long studentId, Long programId);
}
