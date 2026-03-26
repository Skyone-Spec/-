package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminKnowledgeUpsertRequest(
        @NotBlank(message = "标题不能为空") String title,
        @NotBlank(message = "分类不能为空") String category,
        @NotBlank(message = "内容不能为空") String content,
        String officialUrl,
        String sourceFileName,
        String audienceScope,
        @NotBlank(message = "更新人不能为空") String updatedBy,
        @NotNull(message = "发布状态不能为空") Boolean published
) {
}
