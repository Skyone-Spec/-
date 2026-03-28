package edu.ruc.platform.notice.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.notice.service.NoticeApplicationService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeApplicationService noticeService;
    private final CurrentUserService currentUserService;

    @GetMapping("/student/{studentId}")
    public ApiResponse<List<TargetedNoticeResponse>> listForStudent(@Positive(message = "学生ID必须大于 0") @PathVariable Long studentId) {
        currentUserService.requireStudentAccess(studentId, RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success(noticeService.listTargetedNotices(studentId));
    }
}
