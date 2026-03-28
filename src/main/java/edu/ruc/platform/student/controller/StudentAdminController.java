package edu.ruc.platform.student.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.dto.StudentProfileFilterRequest;
import edu.ruc.platform.student.dto.StudentProfileStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitFilterRequest;
import edu.ruc.platform.student.dto.StudentPortraitPageItemResponse;
import edu.ruc.platform.student.dto.StudentPortraitResponse;
import edu.ruc.platform.student.dto.StudentPortraitStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitUpsertRequest;
import edu.ruc.platform.student.dto.StudentProfileUpsertRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryCreateRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryResponse;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
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
@Validated
@RequestMapping("/api/v1/admin/students")
@RequiredArgsConstructor
public class StudentAdminController {

    private final StudentProfileApplicationService studentProfileService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<StudentProfileResponse>> list(@RequestParam(required = false) String grade,
                                                          @RequestParam(required = false) String className) {
        if ((grade != null && !grade.isBlank()) || (className != null && !className.isBlank())) {
            currentUserService.requireStudentScopeOrAdmin(grade, className, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
            return ApiResponse.success(studentProfileService.listStudentsByScope(grade, className));
        }
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY);
        return ApiResponse.success(studentProfileService.listStudents());
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<StudentProfileResponse>> page(@RequestParam(required = false) String grade,
                                                                  @RequestParam(required = false) String className,
                                                                  @RequestParam(required = false) String status,
                                                                  @RequestParam(required = false) String keyword,
                                                                  @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                  @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        if ((grade != null && !grade.isBlank()) || (className != null && !className.isBlank())) {
            currentUserService.requireStudentScopeOrAdmin(grade, className, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        } else {
            currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY);
        }
        return ApiResponse.success(studentProfileService.pageStudents(
                new StudentProfileFilterRequest(grade, className, status, keyword),
                page,
                size
        ));
    }

    @GetMapping("/stats")
    public ApiResponse<StudentProfileStatsResponse> stats(@RequestParam(required = false) String grade,
                                                          @RequestParam(required = false) String className,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(required = false) String keyword) {
        if ((grade != null && !grade.isBlank()) || (className != null && !className.isBlank())) {
            currentUserService.requireStudentScopeOrAdmin(grade, className, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        } else {
            currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY);
        }
        return ApiResponse.success(studentProfileService.studentStats(
                new StudentProfileFilterRequest(grade, className, status, keyword)
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentProfileResponse> detail(@Positive(message = "学生ID必须大于 0") @PathVariable Long id) {
        currentUserService.requireStudentAccess(id, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(studentProfileService.getStudent(id));
    }

    @PostMapping
    public ApiResponse<StudentProfileResponse> create(@Valid @RequestBody StudentProfileUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("学生信息已创建", studentProfileService.createStudent(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StudentProfileResponse> update(@Positive(message = "学生ID必须大于 0") @PathVariable Long id,
                                                      @Valid @RequestBody StudentProfileUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("学生信息已更新", studentProfileService.updateStudent(id, request));
    }

    @GetMapping("/{id}/status-history")
    public ApiResponse<List<StudentStatusHistoryResponse>> statusHistory(@Positive(message = "学生ID必须大于 0") @PathVariable Long id) {
        currentUserService.requireStudentAccess(id, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(studentProfileService.listStatusHistory(id));
    }

    @PostMapping("/{id}/status-history")
    public ApiResponse<StudentStatusHistoryResponse> createStatusHistory(@Positive(message = "学生ID必须大于 0") @PathVariable Long id,
                                                                         @Valid @RequestBody StudentStatusHistoryCreateRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("学生状态变更已登记", studentProfileService.createStatusHistory(id, request));
    }

    @GetMapping("/{id}/portrait")
    public ApiResponse<StudentPortraitResponse> portrait(@Positive(message = "学生ID必须大于 0") @PathVariable Long id) {
        currentUserService.requireStudentAccess(id, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(studentProfileService.getPortrait(id));
    }

    @GetMapping("/portraits/page")
    public ApiResponse<PageResponse<StudentPortraitPageItemResponse>> pagePortraits(@RequestParam(required = false) String grade,
                                                                                     @RequestParam(required = false) String className,
                                                                                     @RequestParam(required = false) Boolean publicVisible,
                                                                                     @RequestParam(required = false) String careerOrientation,
                                                                                     @DecimalMin(value = "0.0", message = "minGpa 不能小于 0") @RequestParam(required = false) Double minGpa,
                                                                                     @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                                     @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        if ((grade != null && !grade.isBlank()) || (className != null && !className.isBlank())) {
            currentUserService.requireStudentScopeOrAdmin(grade, className, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        } else {
            currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY);
        }
        return ApiResponse.success(studentProfileService.pagePortraits(
                new StudentPortraitFilterRequest(grade, className, publicVisible, careerOrientation, minGpa),
                page,
                size
        ));
    }

    @GetMapping("/portraits/stats")
    public ApiResponse<StudentPortraitStatsResponse> portraitStats(@RequestParam(required = false) String grade,
                                                                   @RequestParam(required = false) String className,
                                                                   @RequestParam(required = false) Boolean publicVisible,
                                                                   @RequestParam(required = false) String careerOrientation,
                                                                   @DecimalMin(value = "0.0", message = "minGpa 不能小于 0") @RequestParam(required = false) Double minGpa) {
        if ((grade != null && !grade.isBlank()) || (className != null && !className.isBlank())) {
            currentUserService.requireStudentScopeOrAdmin(grade, className, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        } else {
            currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY);
        }
        return ApiResponse.success(studentProfileService.portraitStats(
                new StudentPortraitFilterRequest(grade, className, publicVisible, careerOrientation, minGpa)
        ));
    }

    @PutMapping("/{id}/portrait")
    public ApiResponse<StudentPortraitResponse> upsertPortrait(@Positive(message = "学生ID必须大于 0") @PathVariable Long id,
                                                               @Valid @RequestBody StudentPortraitUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("学生画像已更新", studentProfileService.upsertPortrait(id, request));
    }
}
