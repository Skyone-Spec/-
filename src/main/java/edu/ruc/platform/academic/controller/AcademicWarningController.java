package edu.ruc.platform.academic.controller;

import edu.ruc.platform.academic.dto.AcademicAnalysisResponse;
import edu.ruc.platform.academic.service.AcademicWarningApplicationService;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/academic")
@RequiredArgsConstructor
public class AcademicWarningController {

    private final AcademicWarningApplicationService academicWarningService;
    private final CurrentUserService currentUserService;

    @GetMapping("/analysis/{studentId}")
    public ApiResponse<AcademicAnalysisResponse> analyze(@Positive(message = "学生ID必须大于 0") @PathVariable Long studentId) {
        currentUserService.requireStudentAccess(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(academicWarningService.analyze(studentId));
    }
}
