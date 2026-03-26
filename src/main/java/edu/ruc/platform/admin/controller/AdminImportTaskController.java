package edu.ruc.platform.admin.controller;

import edu.ruc.platform.admin.dto.DataImportTaskCreateRequest;
import edu.ruc.platform.admin.dto.DataImportErrorFilterRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemCreateRequest;
import edu.ruc.platform.admin.dto.DataImportErrorItemResponse;
import edu.ruc.platform.admin.dto.DataImportTaskFilterRequest;
import edu.ruc.platform.admin.dto.DataImportTaskResponse;
import edu.ruc.platform.admin.dto.DataImportTaskStatsResponse;
import edu.ruc.platform.admin.dto.DataImportTaskUpdateRequest;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/import-tasks")
@RequiredArgsConstructor
public class AdminImportTaskController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<DataImportTaskResponse>> list() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.listImportTasks());
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<DataImportTaskResponse>> page(@RequestParam(required = false) String taskType,
                                                                  @RequestParam(required = false) String status,
                                                                  @RequestParam(required = false) String ownerKeyword,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.pageImportTasks(new DataImportTaskFilterRequest(taskType, status, ownerKeyword), page, size));
    }

    @GetMapping("/stats")
    public ApiResponse<DataImportTaskStatsResponse> stats(@RequestParam(required = false) String taskType,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) String ownerKeyword) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.importTaskStats(new DataImportTaskFilterRequest(taskType, status, ownerKeyword)));
    }

    @PostMapping
    public ApiResponse<DataImportTaskResponse> create(@Valid @RequestBody DataImportTaskCreateRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("导入任务已创建", adminService.createImportTask(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DataImportTaskResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody DataImportTaskUpdateRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("导入任务已更新", adminService.updateImportTask(id, request));
    }

    @GetMapping("/{id}/errors")
    public ApiResponse<List<DataImportErrorItemResponse>> listErrors(@PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.listImportErrors(id));
    }

    @GetMapping("/{id}/errors/page")
    public ApiResponse<PageResponse<DataImportErrorItemResponse>> pageErrors(@PathVariable Long id,
                                                                             @RequestParam(required = false) Integer rowNumber,
                                                                             @RequestParam(required = false) String fieldName,
                                                                             @RequestParam(required = false) String keyword,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(adminService.pageImportErrors(id, new DataImportErrorFilterRequest(rowNumber, fieldName, keyword), page, size));
    }

    @PostMapping("/{id}/errors")
    public ApiResponse<DataImportErrorItemResponse> createError(@PathVariable Long id,
                                                                @Valid @RequestBody DataImportErrorItemCreateRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("导入错误明细已登记", adminService.createImportError(id, request));
    }
}
