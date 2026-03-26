package edu.ruc.platform.platform.dto;

import java.util.List;

public record PlatformUploadPolicyResponse(
        Long maxFileSizeBytes,
        List<String> allowedContentTypes,
        boolean allowEmptyContentType
) {
}
