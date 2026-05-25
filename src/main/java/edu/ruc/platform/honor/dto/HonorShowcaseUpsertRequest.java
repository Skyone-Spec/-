package edu.ruc.platform.honor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record HonorShowcaseUpsertRequest(
        @NotNull(message = "年份不能为空") @Min(value = 1900, message = "年份不合法") Integer awardYear,
        @NotBlank(message = "荣誉类别不能为空") @Size(max = 100, message = "荣誉类别不能超过 100 个字符") String honorCategory,
        @NotBlank(message = "获得者类型不能为空") @Size(max = 32, message = "获得者类型不能超过 32 个字符") String recipientType,
        @NotBlank(message = "标题不能为空") @Size(max = 200, message = "标题不能超过 200 个字符") String title,
        String description,
        Boolean publicVisible,
        Integer displayOrder,
        LocalDateTime displayStartAt,
        LocalDateTime displayEndAt,
        Long importTaskId
) {
}
