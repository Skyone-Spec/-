package edu.ruc.platform.honor.controller;

import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.security.RequireRoles;
import edu.ruc.platform.honor.dto.HonorRecipientAdminResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAttachmentResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAttachmentUpsertRequest;
import edu.ruc.platform.honor.dto.HonorRecipientFilterRequest;
import edu.ruc.platform.honor.dto.HonorRecipientMemberResponse;
import edu.ruc.platform.honor.dto.HonorRecipientMemberUpsertRequest;
import edu.ruc.platform.honor.dto.HonorRecipientUpsertRequest;
import edu.ruc.platform.honor.dto.HonorShowcaseAdminResponse;
import edu.ruc.platform.honor.dto.HonorShowcaseFilterRequest;
import edu.ruc.platform.honor.dto.HonorShowcaseUpsertRequest;
import edu.ruc.platform.honor.service.HonorApplicationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/admin/honors")
@RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR})
@RequiredArgsConstructor
public class AdminHonorController {

    private final HonorApplicationService honorService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<HonorShowcaseAdminResponse>> page(@RequestParam(required = false) Integer awardYear,
                                                                      @RequestParam(required = false) String honorCategory,
                                                                      @RequestParam(required = false) String recipientType,
                                                                      @RequestParam(required = false) Boolean publicVisible,
                                                                      @RequestParam(required = false) String keyword,
                                                                      @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                      @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(honorService.pageAdminShowcases(
                new HonorShowcaseFilterRequest(awardYear, honorCategory, recipientType, publicVisible, keyword),
                page,
                size
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<HonorShowcaseAdminResponse> detail(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id) {
        return ApiResponse.success(honorService.getAdminShowcase(id));
    }

    @PostMapping
    public ApiResponse<HonorShowcaseAdminResponse> create(@Valid @RequestBody HonorShowcaseUpsertRequest request) {
        return ApiResponse.success("荣誉展示模块已创建", honorService.createShowcase(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HonorShowcaseAdminResponse> update(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id,
                                                          @Valid @RequestBody HonorShowcaseUpsertRequest request) {
        return ApiResponse.success("荣誉展示模块已更新", honorService.updateShowcase(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id) {
        honorService.deleteShowcase(id);
        return ApiResponse.success("荣誉展示模块已删除", null);
    }

    @GetMapping("/{id}/recipients/page")
    public ApiResponse<PageResponse<HonorRecipientAdminResponse>> pageRecipients(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id,
                                                                                 @RequestParam(required = false) String recipientType,
                                                                                 @RequestParam(required = false) Boolean publicVisible,
                                                                                 @RequestParam(required = false) String keyword,
                                                                                 @Min(value = 0, message = "page 不能小于 0") @RequestParam(defaultValue = "0") int page,
                                                                                 @Min(value = 1, message = "size 不能小于 1") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(honorService.pageAdminRecipients(
                id,
                new HonorRecipientFilterRequest(recipientType, publicVisible, keyword),
                page,
                size
        ));
    }

    @PostMapping("/{id}/recipients")
    public ApiResponse<HonorRecipientAdminResponse> createRecipient(@Positive(message = "荣誉展示ID必须大于 0") @PathVariable Long id,
                                                                    @Valid @RequestBody HonorRecipientUpsertRequest request) {
        return ApiResponse.success("荣誉获得者已创建", honorService.createRecipient(id, request));
    }

    @GetMapping("/recipients/{recipientId}")
    public ApiResponse<HonorRecipientAdminResponse> recipientDetail(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId) {
        return ApiResponse.success(honorService.getAdminRecipient(recipientId));
    }

    @PutMapping("/recipients/{recipientId}")
    public ApiResponse<HonorRecipientAdminResponse> updateRecipient(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId,
                                                                    @Valid @RequestBody HonorRecipientUpsertRequest request) {
        return ApiResponse.success("荣誉获得者已更新", honorService.updateRecipient(recipientId, request));
    }

    @DeleteMapping("/recipients/{recipientId}")
    public ApiResponse<Void> deleteRecipient(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId) {
        honorService.deleteRecipient(recipientId);
        return ApiResponse.success("荣誉获得者已删除", null);
    }

    @PostMapping("/recipients/{recipientId}/members")
    public ApiResponse<HonorRecipientMemberResponse> createMember(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId,
                                                                  @Valid @RequestBody HonorRecipientMemberUpsertRequest request) {
        return ApiResponse.success("荣誉集体成员已创建", honorService.createMember(recipientId, request));
    }

    @PutMapping("/members/{memberId}")
    public ApiResponse<HonorRecipientMemberResponse> updateMember(@Positive(message = "荣誉成员ID必须大于 0") @PathVariable Long memberId,
                                                                  @Valid @RequestBody HonorRecipientMemberUpsertRequest request) {
        return ApiResponse.success("荣誉集体成员已更新", honorService.updateMember(memberId, request));
    }

    @DeleteMapping("/members/{memberId}")
    public ApiResponse<Void> deleteMember(@Positive(message = "荣誉成员ID必须大于 0") @PathVariable Long memberId) {
        honorService.deleteMember(memberId);
        return ApiResponse.success("荣誉集体成员已删除", null);
    }

    @PostMapping("/recipients/{recipientId}/attachments")
    public ApiResponse<HonorRecipientAttachmentResponse> createAttachment(@Positive(message = "荣誉获得者ID必须大于 0") @PathVariable Long recipientId,
                                                                          @Valid @RequestBody HonorRecipientAttachmentUpsertRequest request) {
        return ApiResponse.success("荣誉附件已创建", honorService.createAttachment(recipientId, request));
    }

    @PutMapping("/attachments/{attachmentId}")
    public ApiResponse<HonorRecipientAttachmentResponse> updateAttachment(@Positive(message = "荣誉附件ID必须大于 0") @PathVariable Long attachmentId,
                                                                          @Valid @RequestBody HonorRecipientAttachmentUpsertRequest request) {
        return ApiResponse.success("荣誉附件已更新", honorService.updateAttachment(attachmentId, request));
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@Positive(message = "荣誉附件ID必须大于 0") @PathVariable Long attachmentId) {
        honorService.deleteAttachment(attachmentId);
        return ApiResponse.success("荣誉附件已删除", null);
    }
}
