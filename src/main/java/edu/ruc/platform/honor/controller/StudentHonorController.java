package edu.ruc.platform.honor.controller;

import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.security.RequireRoles;
import edu.ruc.platform.honor.dto.HonorRecipientStudentResponse;
import edu.ruc.platform.honor.dto.HonorShowcaseFilterRequest;
import edu.ruc.platform.honor.dto.HonorShowcaseStudentResponse;
import edu.ruc.platform.honor.service.HonorApplicationService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/student/honors")
@RequireRoles({
        RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR,
        RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY, RoleType.STUDENT
})
@RequiredArgsConstructor
public class StudentHonorController {

    private final HonorApplicationService honorService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<HonorShowcaseStudentResponse>> page(@RequestParam(required = false) Integer awardYear,
                                                                        @RequestParam(required = false) String honorCategory,
                                                                        @RequestParam(required = false) String recipientType,
                                                                        @RequestParam(required = false) String keyword,
                                                                        @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                        @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(honorService.pageStudentShowcases(
                new HonorShowcaseFilterRequest(awardYear, honorCategory, recipientType, null, keyword),
                page,
                size
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<HonorShowcaseStudentResponse> detail(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id) {
        return ApiResponse.success(honorService.getStudentShowcase(id));
    }

    @GetMapping("/{id}/recipients")
    public ApiResponse<List<HonorRecipientStudentResponse>> recipients(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id) {
        return ApiResponse.success(honorService.listStudentRecipients(id));
    }

    @GetMapping("/recipients/{recipientId}")
    public ApiResponse<HonorRecipientStudentResponse> recipientDetail(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId) {
        return ApiResponse.success(honorService.getStudentRecipient(recipientId));
    }
}
