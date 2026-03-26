package edu.ruc.platform.admin.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.certificate.dto.ApprovalActionRequest;
import edu.ruc.platform.certificate.dto.ApprovalHistoryResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskFilterRequest;
import edu.ruc.platform.certificate.dto.ApprovalTaskStatsResponse;
import edu.ruc.platform.certificate.dto.ApprovalTaskResponse;
import edu.ruc.platform.certificate.service.CertificateApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalController {

    private final CertificateApplicationService certificateService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<ApprovalTaskResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(certificateService.listApprovalTasks());
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<ApprovalTaskResponse>> page(@RequestParam(required = false) Long studentId,
                                                                @RequestParam(required = false) String status,
                                                                @RequestParam(required = false) String certificateType,
                                                                @RequestParam(required = false) String keyword,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(certificateService.pageApprovalTasks(
                new ApprovalTaskFilterRequest(studentId, status, certificateType, keyword),
                page,
                size
        ));
    }

    @GetMapping("/stats")
    public ApiResponse<ApprovalTaskStatsResponse> stats(@RequestParam(required = false) Long studentId,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String certificateType,
                                                        @RequestParam(required = false) String keyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(certificateService.approvalTaskStats(
                new ApprovalTaskFilterRequest(studentId, status, certificateType, keyword)
        ));
    }

    @GetMapping("/{requestId}/history")
    public ApiResponse<List<ApprovalHistoryResponse>> history(@PathVariable Long requestId) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(certificateService.listApprovalHistory(requestId));
    }

    @PostMapping("/{requestId}/action")
    public ApiResponse<ApprovalTaskResponse> action(@PathVariable Long requestId,
                                                    @Valid @RequestBody ApprovalActionRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("审批已处理", certificateService.handleApproval(requestId, request));
    }
}
