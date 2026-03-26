package edu.ruc.platform.platform.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.domain.PlatformSystemSetting;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformUploadPolicyUpdateRequest;
import edu.ruc.platform.platform.repository.PlatformSystemSettingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Profile("!mock")
public class DefaultPlatformUploadPolicyService implements PlatformUploadPolicyService {

    private static final String KEY_MAX_FILE_SIZE_BYTES = "platform.upload.max-file-size-bytes";
    private static final String KEY_ALLOWED_CONTENT_TYPES = "platform.upload.allowed-content-types";
    private static final String KEY_ALLOW_EMPTY_CONTENT_TYPE = "platform.upload.allow-empty-content-type";

    private final PlatformSystemSettingRepository platformSystemSettingRepository;
    private final long defaultMaxFileSizeBytes;
    private final String defaultAllowedContentTypes;
    private final boolean defaultAllowEmptyContentType;

    public DefaultPlatformUploadPolicyService(
            PlatformSystemSettingRepository platformSystemSettingRepository,
            @Value("${platform.upload.max-file-size-bytes:20971520}") long maxFileSizeBytes,
            @Value("${platform.upload.allowed-content-types:application/pdf,image/png,image/jpeg,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel}") String allowedContentTypes,
            @Value("${platform.upload.allow-empty-content-type:false}") boolean allowEmptyContentType) {
        this.platformSystemSettingRepository = platformSystemSettingRepository;
        this.defaultMaxFileSizeBytes = maxFileSizeBytes;
        this.defaultAllowedContentTypes = allowedContentTypes;
        this.defaultAllowEmptyContentType = allowEmptyContentType;
    }

    @Override
    public synchronized PlatformUploadPolicyResponse getPolicy() {
        return new PlatformUploadPolicyResponse(maxFileSizeBytes(), allowedContentTypes(), allowEmptyContentType());
    }

    @Override
    public synchronized PlatformUploadPolicyResponse updatePolicy(PlatformUploadPolicyUpdateRequest request) {
        if (request.maxFileSizeBytes() != null) {
            if (request.maxFileSizeBytes() <= 0) {
                throw new BusinessException("上传大小限制必须大于 0");
            }
            saveSetting(KEY_MAX_FILE_SIZE_BYTES, String.valueOf(request.maxFileSizeBytes()), "平台上传单文件大小限制");
        }
        if (request.allowedContentTypes() != null) {
            if (request.allowedContentTypes().isEmpty()) {
                throw new BusinessException("允许的文件类型不能为空");
            }
            saveSetting(KEY_ALLOWED_CONTENT_TYPES, String.join(",", request.allowedContentTypes()), "平台允许上传的内容类型");
        }
        if (request.allowEmptyContentType() != null) {
            saveSetting(KEY_ALLOW_EMPTY_CONTENT_TYPE, String.valueOf(request.allowEmptyContentType()), "平台是否允许空内容类型");
        }
        return getPolicy();
    }

    @Override
    public synchronized long maxFileSizeBytes() {
        String value = platformSystemSettingRepository.findBySettingKey(KEY_MAX_FILE_SIZE_BYTES)
                .map(PlatformSystemSetting::getSettingValue)
                .orElse(String.valueOf(defaultMaxFileSizeBytes));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException("平台配置格式错误: " + KEY_MAX_FILE_SIZE_BYTES);
        }
    }

    @Override
    public synchronized List<String> allowedContentTypes() {
        String value = platformSystemSettingRepository.findBySettingKey(KEY_ALLOWED_CONTENT_TYPES)
                .map(PlatformSystemSetting::getSettingValue)
                .orElse(defaultAllowedContentTypes);
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    @Override
    public synchronized boolean allowEmptyContentType() {
        String value = platformSystemSettingRepository.findBySettingKey(KEY_ALLOW_EMPTY_CONTENT_TYPE)
                .map(PlatformSystemSetting::getSettingValue)
                .orElse(String.valueOf(defaultAllowEmptyContentType));
        return Boolean.parseBoolean(value);
    }

    private void saveSetting(String key, String value, String description) {
        PlatformSystemSetting setting = platformSystemSettingRepository.findBySettingKey(key)
                .orElseGet(PlatformSystemSetting::new);
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setDescription(description);
        platformSystemSettingRepository.save(setting);
    }
}
