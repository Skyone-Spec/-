package edu.ruc.platform.student.dto;

import jakarta.validation.constraints.NotBlank;

public record StudentStatusHistoryCreateRequest(
        @NotBlank(message = "状态不能为空") String status,
        String changedToMajor,
        String reason
) {
}
