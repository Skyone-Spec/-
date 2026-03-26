package edu.ruc.platform.platform.dto;

import java.util.List;

public record PlatformStudentUiContractResponse(
        List<String> actionTypes,
        List<String> actionPriorities,
        List<PlatformStudentUiActionMetaResponse> actions
) {
}
