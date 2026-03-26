package edu.ruc.platform.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AdminNoticeCreateRequest(
        @NotBlank(message = "标题不能为空") String title,
        @NotBlank(message = "摘要不能为空") String summary,
        @NotEmpty(message = "标签不能为空") List<String> tags,
        @NotBlank(message = "目标人群不能为空") String targetDescription
) {
}
