package edu.ruc.platform.admin.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.admin.dto.AdminNoticeCreateRequest;
import edu.ruc.platform.admin.dto.AdminNoticeFilterRequest;
import edu.ruc.platform.admin.dto.AdminNoticeStatsResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<TargetedNoticeResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.listNotices());
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<TargetedNoticeResponse>> page(@RequestParam(required = false) String keyword,
                                                                  @RequestParam(required = false) String tag,
                                                                  @RequestParam(required = false) String targetKeyword,
                                                                  @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                  @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.pageNotices(new AdminNoticeFilterRequest(keyword, tag, targetKeyword), page, size));
    }

    @GetMapping("/stats")
    public ApiResponse<AdminNoticeStatsResponse> stats(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String tag,
                                                       @RequestParam(required = false) String targetKeyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.noticeStats(new AdminNoticeFilterRequest(keyword, tag, targetKeyword)));
    }

    @PostMapping
    public ApiResponse<TargetedNoticeResponse> create(@Valid @RequestBody AdminNoticeCreateRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("通知已创建", adminService.createNotice(request));
    }
}
