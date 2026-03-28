package edu.ruc.platform.admin.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.admin.dto.AdminKnowledgeFilterRequest;
import edu.ruc.platform.admin.dto.AdminKnowledgeItemResponse;
import edu.ruc.platform.admin.dto.AdminKnowledgeStatsResponse;
import edu.ruc.platform.admin.dto.AdminKnowledgeUpsertRequest;
import edu.ruc.platform.admin.dto.KnowledgeAttachmentResponse;
import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/admin/knowledge")
@RequiredArgsConstructor
public class AdminKnowledgeController {

    private final AdminApplicationService adminService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<AdminKnowledgeItemResponse>> list() {
        return ApiResponse.success(adminService.listKnowledgeItems(
                currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY)
        ));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<AdminKnowledgeItemResponse>> page(@RequestParam(required = false) String keyword,
                                                                      @RequestParam(required = false) String category,
                                                                      @RequestParam(required = false) Boolean published,
                                                                      @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                      @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(adminService.pageKnowledgeItems(
                currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY),
                new AdminKnowledgeFilterRequest(keyword, category, published),
                page,
                size
        ));
    }

    @GetMapping("/stats")
    public ApiResponse<AdminKnowledgeStatsResponse> stats(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) String category,
                                                          @RequestParam(required = false) Boolean published) {
        return ApiResponse.success(adminService.knowledgeStats(
                currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR, RoleType.LEAGUE_SECRETARY),
                new AdminKnowledgeFilterRequest(keyword, category, published)
        ));
    }

    @PostMapping
    public ApiResponse<AdminKnowledgeItemResponse> create(@Valid @RequestBody AdminKnowledgeUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("知识条目已创建", adminService.createKnowledgeItem(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminKnowledgeItemResponse> update(@Positive(message = "知识条目ID必须大于 0") @PathVariable Long id,
                                                          @Valid @RequestBody AdminKnowledgeUpsertRequest request) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("知识条目已更新", adminService.updateKnowledgeItem(id, request));
    }

    @GetMapping("/{id}/attachments")
    public ApiResponse<List<KnowledgeAttachmentResponse>> listAttachments(@Positive(message = "知识条目ID必须大于 0") @PathVariable Long id) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.CLASS_ADVISOR);
        return ApiResponse.success(adminService.listKnowledgeAttachments(id));
    }

    @PostMapping("/{id}/attachments")
    public ApiResponse<KnowledgeAttachmentResponse> uploadAttachment(@Positive(message = "知识条目ID必须大于 0") @PathVariable Long id,
                                                                     @RequestParam("file") MultipartFile file) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        return ApiResponse.success("知识附件已上传", adminService.uploadKnowledgeAttachment(id, file));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@Positive(message = "附件ID必须大于 0") @PathVariable Long attachmentId) {
        currentUserService.requireAnyRole(RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR);
        adminService.deleteKnowledgeAttachment(attachmentId);
        return ApiResponse.success("知识附件已删除", null);
    }
}
