package edu.ruc.platform.certificate.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import edu.ruc.platform.certificate.dto.CertificatePreviewResponse;
import edu.ruc.platform.certificate.dto.CertificateRequestActionRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestCreateRequest;
import edu.ruc.platform.certificate.dto.CertificateRequestResponse;
import edu.ruc.platform.certificate.service.CertificateApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateApplicationService certificateService;
    private final CurrentUserService currentUserService;

    @PostMapping("/requests")
    public ApiResponse<CertificateRequestResponse> create(@Valid @RequestBody CertificateRequestCreateRequest request) {
        currentUserService.requireSelfOrAdmin(request.studentId(), RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("申请已提交", certificateService.create(request));
    }

    @GetMapping("/requests/student/{studentId}")
    public ApiResponse<List<CertificateRequestResponse>> listByStudentId(@Positive(message = "学生ID必须大于 0") @PathVariable Long studentId) {
        currentUserService.requireSelfOrAdmin(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(certificateService.listByStudentId(studentId));
    }

    @GetMapping("/requests/{requestId}/history")
    public ApiResponse<List<ApprovalHistoryResponse>> history(@Positive(message = "申请ID必须大于 0") @PathVariable Long requestId) {
        return ApiResponse.success(certificateService.listRequestHistory(requestId));
    }

    @GetMapping("/requests/{requestId}/preview")
    public ApiResponse<CertificatePreviewResponse> preview(@Positive(message = "申请ID必须大于 0") @PathVariable Long requestId) {
        return ApiResponse.success(certificateService.preview(requestId));
    }

    @PostMapping("/requests/{requestId}/action")
    public ApiResponse<CertificateRequestResponse> action(@Positive(message = "申请ID必须大于 0") @PathVariable Long requestId,
                                                          @Valid @RequestBody CertificateRequestActionRequest request) {
        return ApiResponse.success("申请状态已更新", certificateService.handleStudentAction(requestId, request));
    }
}
