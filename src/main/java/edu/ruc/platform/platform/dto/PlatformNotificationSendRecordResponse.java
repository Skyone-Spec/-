package edu.ruc.platform.platform.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlatformNotificationSendRecordResponse(
        Long id,
        String title,
        String channel,
        String targetType,
        String targetDescription,
        String status,
        Integer recipientCount,
        String triggeredBy,
        LocalDateTime sentAt,
        List<String> extensionChannels
) {
}
