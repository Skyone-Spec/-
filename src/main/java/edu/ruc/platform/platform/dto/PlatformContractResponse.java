package edu.ruc.platform.platform.dto;

import java.util.List;
import java.util.Map;

public record PlatformContractResponse(
        String userIdField,
        String studentIdField,
        String studentNoField,
        String permissionCheckEntry,
        String uploadResponseType,
        String pageRequest,
        String pageResponse,
        List<String> roleEnums,
        List<String> dataScopes,
        List<String> approvalStatuses,
        List<String> importTaskStatuses,
        List<String> notificationChannels,
        List<String> notificationTargetTypes,
        List<String> notificationSendStatuses,
        List<String> approvalLogFields,
        List<String> operationLogFields,
        List<String> studentActionTypes,
        List<String> studentActionPriorities,
        Map<String, String> studentActionPaths
) {
}
