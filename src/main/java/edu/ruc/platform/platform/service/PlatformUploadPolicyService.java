package edu.ruc.platform.platform.service;

import edu.ruc.platform.platform.dto.PlatformUploadPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyUpdateRequest;

import java.util.List;

public interface PlatformUploadPolicyService {

    PlatformUploadPolicyResponse getPolicy();

    PlatformUploadPolicyResponse updatePolicy(PlatformUploadPolicyUpdateRequest request);

    long maxFileSizeBytes();

    List<String> allowedContentTypes();

    boolean allowEmptyContentType();
}
