package edu.ruc.platform.certificate.controller;

import edu.ruc.platform.certificate.dto.*;
import edu.ruc.platform.certificate.service.CertificateTemplateApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.security.RequireRoles;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/certificate-templates")
@RequiredArgsConstructor
public class CertificateTemplateController {

    private final CertificateTemplateApplicationService templateService;

    @GetMapping
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<List<CertificateTemplateResponse>> listAll() {
        return ApiResponse.success(templateService.listAll());
    }

    @GetMapping("/active")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
            RoleType.CLASS_ADVISOR, RoleType.STUDENT})
    public ApiResponse<List<CertificateTemplateResponse>> listActive() {
        return ApiResponse.success(templateService.listActive());
    }

    @GetMapping("/type/{type}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<List<CertificateTemplateResponse>> listByType(@PathVariable String type) {
        return ApiResponse.success(templateService.listByType(type));
    }

    @GetMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<CertificateTemplateResponse> getById(@Positive(message = "模板ID必须大于0") @PathVariable Long id) {
        return ApiResponse.success(templateService.getById(id));
    }

    @GetMapping("/code/{code}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<CertificateTemplateResponse> getByCode(@PathVariable String code) {
        return ApiResponse.success(templateService.getByCode(code));
    }

    @PostMapping
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<CertificateTemplateResponse> create(@Valid @RequestBody CertificateTemplateCreateRequest request) {
        return ApiResponse.success("模板创建成功", templateService.create(request));
    }

    @PutMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<CertificateTemplateResponse> update(@Positive(message = "模板ID必须大于0") @PathVariable Long id,
                                                           @Valid @RequestBody CertificateTemplateUpdateRequest request) {
        return ApiResponse.success("模板更新成功", templateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<Void> delete(@Positive(message = "模板ID必须大于0") @PathVariable Long id) {
        templateService.delete(id);
        return ApiResponse.success("模板删除成功", null);
    }

    @GetMapping("/{id}/preview/student/{studentId}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
    public ApiResponse<String> renderPreview(@Positive(message = "模板ID必须大于0") @PathVariable Long id,
                                            @Positive(message = "学生ID必须大于0") @PathVariable Long studentId) {
        return ApiResponse.success(templateService.renderPreview(id, studentId));
    }
}
