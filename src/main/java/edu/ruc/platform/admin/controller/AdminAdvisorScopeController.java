package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.AdvisorScopeBindingResponse;
import edu.ruc.platform.admin.dto.AdvisorScopeFilterRequest;
import edu.ruc.platform.admin.dto.AdvisorScopeBindingUpsertRequest;
import edu.ruc.platform.admin.dto.AdvisorScopeStatsResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/advisor-scopes")
@RequiredArgsConstructor
public class AdminAdvisorScopeController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<AdvisorScopeBindingResponse>> list(@RequestParam(required = false) String advisorUsername,
                                                               @RequestParam(required = false) String grade,
                                                               @RequestParam(required = false) String className) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.listAdvisorScopes(advisorUsername, grade, className));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<AdvisorScopeBindingResponse>> page(@RequestParam(required = false) String advisorUsername,
                                                                       @RequestParam(required = false) String grade,
                                                                       @RequestParam(required = false) String className,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.pageAdvisorScopes(new AdvisorScopeFilterRequest(advisorUsername, grade, className), page, size));
    }

    @GetMapping("/stats")
    public ApiResponse<AdvisorScopeStatsResponse> stats(@RequestParam(required = false) String advisorUsername,
                                                        @RequestParam(required = false) String grade,
                                                        @RequestParam(required = false) String className) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.advisorScopeStats(new AdvisorScopeFilterRequest(advisorUsername, grade, className)));
    }

    @PostMapping
    public ApiResponse<AdvisorScopeBindingResponse> create(@Valid @RequestBody AdvisorScopeBindingUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("班主任负责范围已创建", adminService.createAdvisorScope(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdvisorScopeBindingResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody AdvisorScopeBindingUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("班主任负责范围已更新", adminService.updateAdvisorScope(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        adminService.deleteAdvisorScope(id);
        return ApiResponse.success("班主任负责范围已删除", null);
    }
}
