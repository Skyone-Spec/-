package edu.ruc.platform.admin.dto;

public record AdminOperationLogFilterRequest(
        String module,
        String action,
        String operatorRole,
        String targetKeyword
) {
}
