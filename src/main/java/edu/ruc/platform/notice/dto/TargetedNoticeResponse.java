package edu.ruc.platform.notice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TargetedNoticeResponse(
        Long id,
        String title,
        String summary,
        List<String> tags,
        String targetDescription,
        String priority,
        List<String> matchedRules,
        List<String> deliveryChannels,
        LocalDateTime publishTime
) {
}
