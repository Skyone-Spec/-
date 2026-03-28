package edu.ruc.platform.worklog.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.worklog.dto.StudentWorkLogCreateRequest;
import edu.ruc.platform.worklog.dto.StudentWorkLogResponse;
import edu.ruc.platform.worklog.dto.StudentWorkLogUpdateRequest;
import edu.ruc.platform.worklog.dto.StudentWorkloadSummaryResponse;
import edu.ruc.platform.worklog.dto.WorklogAdminStatsResponse;
import edu.ruc.platform.worklog.dto.WorklogActionLogResponse;
import edu.ruc.platform.worklog.dto.WorklogExportMetadataResponse;
import edu.ruc.platform.worklog.dto.WorklogFilterRequest;
import edu.ruc.platform.worklog.dto.WorklogOverviewResponse;
import edu.ruc.platform.worklog.service.StudentWorkLogApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/worklogs")
@RequiredArgsConstructor
public class StudentWorkLogController {

    private final StudentWorkLogApplicationService service;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ApiResponse<StudentWorkLogResponse> create(@Valid @RequestBody StudentWorkLogCreateRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY);
        return ApiResponse.success("工作记录已创建", service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StudentWorkLogResponse> update(@Positive(message = "工作记录ID必须大于 0") @PathVariable Long id,
                                                      @Valid @RequestBody StudentWorkLogUpdateRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY);
        return ApiResponse.success("工作记录已更新", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Positive(message = "工作记录ID必须大于 0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY);
        service.delete(id);
        return ApiResponse.success("工作记录已删除", null);
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<List<StudentWorkLogResponse>> listByStudent(@Positive(message = "学生ID必须大于 0") @PathVariable Long studentId) {
        currentUserService.requireStudentAccess(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.listByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/summary")
    public ApiResponse<StudentWorkloadSummaryResponse> summarize(@Positive(message = "学生ID必须大于 0") @PathVariable Long studentId) {
        currentUserService.requireStudentAccess(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(service.summarize(studentId));
    }

    @GetMapping("/{id}/actions")
    public ApiResponse<List<WorklogActionLogResponse>> actions(@Positive(message = "工作记录ID必须大于 0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(service.listActionLogs(id));
    }

    @GetMapping("/overview")
    public ApiResponse<WorklogOverviewResponse> overview() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(service.overview());
    }

    @GetMapping("/admin/filter")
    public ApiResponse<List<StudentWorkLogResponse>> filter(@Positive(message = "学生ID必须大于 0") @RequestParam(required = false) Long studentId,
                                                            @RequestParam(required = false) String category,
                                                            @RequestParam(required = false) String recorderRole,
                                                            @RequestParam(required = false) String grade,
                                                            @RequestParam(required = false) String className,
                                                            @RequestParam(required = false) LocalDate startDate,
                                                            @RequestParam(required = false) LocalDate endDate,
                                                            @RequestParam(defaultValue = "workDate") String sortBy,
                                                            @RequestParam(defaultValue = "desc") String sortDir) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(service.filter(new WorklogFilterRequest(studentId, category, recorderRole, grade, className, startDate, endDate), sortBy, sortDir));
    }

    @GetMapping("/admin/stats")
    public ApiResponse<WorklogAdminStatsResponse> adminStats(@Positive(message = "学生ID必须大于 0") @RequestParam(required = false) Long studentId,
                                                             @RequestParam(required = false) String category,
                                                             @RequestParam(required = false) String recorderRole,
                                                             @RequestParam(required = false) String grade,
                                                             @RequestParam(required = false) String className,
                                                             @RequestParam(required = false) LocalDate startDate,
                                                             @RequestParam(required = false) LocalDate endDate) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(service.adminStats(new WorklogFilterRequest(studentId, category, recorderRole, grade, className, startDate, endDate)));
    }

    @GetMapping("/admin/page")
    public ApiResponse<PageResponse<StudentWorkLogResponse>> adminPage(@Positive(message = "学生ID必须大于 0") @RequestParam(required = false) Long studentId,
                                                                       @RequestParam(required = false) String category,
                                                                       @RequestParam(required = false) String recorderRole,
                                                                       @RequestParam(required = false) String grade,
                                                                       @RequestParam(required = false) String className,
                                                                       @RequestParam(required = false) LocalDate startDate,
                                                                       @RequestParam(required = false) LocalDate endDate,
                                                                       @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                       @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size,
                                                                       @RequestParam(defaultValue = "workDate") String sortBy,
                                                                       @RequestParam(defaultValue = "desc") String sortDir) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(service.adminPage(new WorklogFilterRequest(studentId, category, recorderRole, grade, className, startDate, endDate), page, size, sortBy, sortDir));
    }

    @GetMapping("/admin/export-metadata")
    public ApiResponse<WorklogExportMetadataResponse> exportMetadata() {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(service.exportMetadata());
    }
}
