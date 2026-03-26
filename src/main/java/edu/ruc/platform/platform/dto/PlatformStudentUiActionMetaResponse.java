package edu.ruc.platform.platform.dto;

public record PlatformStudentUiActionMetaResponse(
        String type,
        String defaultPriority,
        String actionPath,
        String iconKey,
        String colorKey
) {
}
