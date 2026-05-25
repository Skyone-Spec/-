package edu.ruc.platform.party.controller;

import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.security.RequireRoles;
import edu.ruc.platform.party.dto.*;
import edu.ruc.platform.party.service.PartyFlowApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/party/flows")
@RequiredArgsConstructor
public class PartyFlowController {

    private final PartyFlowApplicationService flowService;

    @GetMapping
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.STUDENT})
    public ApiResponse<List<PartyFlowTemplateResponse>> listAll() {
        return ApiResponse.success(flowService.listAllTemplates());
    }

    @GetMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.STUDENT})
    public ApiResponse<PartyFlowTemplateResponse> getById(@Positive(message = "流程ID必须大于0") @PathVariable Long id) {
        return ApiResponse.success(flowService.getTemplateById(id));
    }

    @PostMapping
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<PartyFlowTemplateResponse> create(@Valid @RequestBody PartyFlowTemplateCreateRequest request) {
        return ApiResponse.success("流程模板创建成功", flowService.createTemplate(request));
    }

    @PostMapping("/{id}/stages")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<PartyFlowStageResponse> addStage(@Positive(message = "流程ID必须大于0") @PathVariable Long id,
                                                       @Valid @RequestBody PartyFlowStageCreateRequest request) {
        return ApiResponse.success("阶段添加成功", flowService.addStage(id, request));
    }

    @DeleteMapping("/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<Void> delete(@Positive(message = "流程ID必须大于0") @PathVariable Long id) {
        flowService.deleteTemplate(id);
        return ApiResponse.success("流程模板删除成功", null);
    }
}
