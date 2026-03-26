package edu.ruc.platform.certificate.service;

import edu.ruc.platform.certificate.dto.ApprovalActionRequest;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskFilterRequest;
import edu.ruc.platform.certificate.dto.ApprovalTaskStatsResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskResponse;
import edu.ruc.platform.certificate.dto.CertificatePreviewResponse;
import edu.ruc.platform.certificate.dto.CertificateRequestActionRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestCreateRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.common.api.PageResponse;

import java.util.List;

public interface CertificateApplicationService {

    CertificateRequestResponse create(CertificateRequestCreateRequest request);

    List<CertificateRequestResponse> listByStudentId(Long studentId);

    List<ApprovalTaskResponse> listApprovalTasks();

    PageResponse<ApprovalTaskResponse> pageApprovalTasks(ApprovalTaskFilterRequest request, int page, int size);

    ApprovalTaskStatsResponse approvalTaskStats(ApprovalTaskFilterRequest request);

    ApprovalTaskResponse handleApproval(Long requestId, ApprovalActionRequest request);

    List<ApprovalHistoryResponse> listApprovalHistory(Long requestId);

    List<ApprovalHistoryResponse> listRequestHistory(Long requestId);

    CertificatePreviewResponse preview(Long requestId);

    CertificateRequestResponse handleStudentAction(Long requestId, CertificateRequestActionRequest request);
}
