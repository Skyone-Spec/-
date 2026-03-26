package edu.ruc.platform.platform.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyUpdateRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("mock")
public class MockPlatformUploadPolicyService implements PlatformUploadPolicyService {

    private long maxFileSizeBytes = 20L * 1024 * 1024;
    private List<String> allowedContentTypes = new ArrayList<>(List.of(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel"
    ));
    private boolean allowEmptyContentType = false;

    @Override
    public synchronized PlatformUploadPolicyResponse getPolicy() {
        return new PlatformUploadPolicyResponse(maxFileSizeBytes, List.copyOf(allowedContentTypes), allowEmptyContentType);
    }

    @Override
    public synchronized PlatformUploadPolicyResponse updatePolicy(PlatformUploadPolicyUpdateRequest request) {
        if (request.maxFileSizeBytes() != null) {
            if (request.maxFileSizeBytes() <= 0) {
                throw new BusinessException("上传大小限制必须大于 0");
            }
            maxFileSizeBytes = request.maxFileSizeBytes();
        }
        if (request.allowedContentTypes() != null) {
            if (request.allowedContentTypes().isEmpty()) {
                throw new BusinessException("允许的文件类型不能为空");
            }
            allowedContentTypes = new ArrayList<>(request.allowedContentTypes());
        }
        if (request.allowEmptyContentType() != null) {
            allowEmptyContentType = request.allowEmptyContentType();
        }
        return getPolicy();
    }

    @Override
    public synchronized long maxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    @Override
    public synchronized List<String> allowedContentTypes() {
        return List.copyOf(allowedContentTypes);
    }

    @Override
    public synchronized boolean allowEmptyContentType() {
        return allowEmptyContentType;
    }
}
