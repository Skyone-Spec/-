package edu.ruc.platform.academic.controller;

import edu.ruc.platform.academic.dto.*;
import edu.ruc.platform.academic.service.AcademicProgramApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.security.RequireRoles;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/academic/programs")
@RequiredArgsConstructor
public class AcademicProgramController {

    private final AcademicProgramApplicationService programService;

    @GetMapping
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<List<AcademicProgramResponse>> listAll() {
        return ApiResponse.success(programService.listAll());
    }

    @GetMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<AcademicProgramResponse> getById(@Positive(message = "培养方案ID必须大于0") @PathVariable Long id) {
        return ApiResponse.success(programService.getById(id));
    }

    @PostMapping
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<AcademicProgramResponse> create(@Valid @RequestBody AcademicProgramCreateRequest request) {
        return ApiResponse.success("培养方案创建成功", programService.create(request));
    }

    @PutMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<AcademicProgramResponse> update(@Positive(message = "培养方案ID必须大于0") @PathVariable Long id,
                                                      @Valid @RequestBody AcademicProgramCreateRequest request) {
        return ApiResponse.success("培养方案更新成功", programService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<Void> delete(@Positive(message = "培养方案ID必须大于0") @PathVariable Long id) {
        programService.delete(id);
        return ApiResponse.success("培养方案删除成功", null);
    }

    @PostMapping("/{id}/modules")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<AcademicProgramModuleResponse> addModule(@Positive(message = "培养方案ID必须大于0") @PathVariable Long id,
                                                               @Valid @RequestBody AcademicProgramModuleCreateRequest request) {
        return ApiResponse.success("模块添加成功", programService.addModule(id, request));
    }

    @DeleteMapping("/modules/{moduleId}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<Void> removeModule(@Positive(message = "模块ID必须大于0") @PathVariable Long moduleId) {
        programService.removeModule(moduleId);
        return ApiResponse.success("模块删除成功", null);
    }

    @PostMapping("/transcripts/student/{studentId}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<TranscriptUploadResponse> uploadTranscript(@Positive(message = "学生ID必须大于0") @PathVariable Long studentId,
                                                                 @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("成绩单上传成功", programService.uploadTranscript(studentId, file));
    }

    @GetMapping("/transcripts/student/{studentId}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.STUDENT})
    public ApiResponse<TranscriptUploadResponse> getTranscript(@Positive(message = "学生ID必须大于0") @PathVariable Long studentId) {
        return ApiResponse.success(programService.getTranscript(studentId));
    }

    @GetMapping("/audit-report/student/{studentId}/program/{programId}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.STUDENT})
    public ApiResponse<AuditReportResponse> generateAuditReport(@Positive(message = "学生ID必须大于0") @PathVariable Long studentId,
                                                               @Positive(message = "培养方案ID必须大于0") @PathVariable Long programId) {
        return ApiResponse.success(programService.generateAuditReport(studentId, programId));
    }
}
