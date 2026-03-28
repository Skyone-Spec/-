package edu.ruc.platform.knowledge.controller;

import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeDetailResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.knowledge.service.KnowledgeApplicationService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeApplicationService knowledgeService;

    @GetMapping("/search")
    public ApiResponse<List<KnowledgeSearchResponse>> search(@RequestParam String keyword) {
        return ApiResponse.success(knowledgeService.search(keyword));
    }

    @GetMapping("/templates")
    public ApiResponse<List<KnowledgeSearchResponse>> listTemplates() {
        return ApiResponse.success(knowledgeService.listTemplates());
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeDetailResponse> detail(@Positive(message = "知识条目ID必须大于 0") @PathVariable Long id) {
        return ApiResponse.success(knowledgeService.getDetail(id));
    }
}
