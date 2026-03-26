package edu.ruc.platform.admin.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.admin.dto.AdminOperationLogFilterRequest;
import edu.ruc.platform.admin.dto.AdminOperationLogResponse;
import edu.ruc.platform.admin.dto.AdminOperationLogStatsResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/operation-logs")
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<AdminOperationLogResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.listOperationLogs());
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<AdminOperationLogResponse>> page(@RequestParam(required = false) String module,
                                                                     @RequestParam(required = false) String action,
                                                                     @RequestParam(required = false) String operatorRole,
                                                                     @RequestParam(required = false) String targetKeyword,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.pageOperationLogs(
                new AdminOperationLogFilterRequest(module, action, operatorRole, targetKeyword),
                page,
                size
        ));
    }

    @GetMapping("/stats")
    public ApiResponse<AdminOperationLogStatsResponse> stats(@RequestParam(required = false) String module,
                                                             @RequestParam(required = false) String action,
                                                             @RequestParam(required = false) String operatorRole,
                                                             @RequestParam(required = false) String targetKeyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN);
        return ApiResponse.success(adminService.operationLogStats(
                new AdminOperationLogFilterRequest(module, action, operatorRole, targetKeyword)
        ));
    }
}
