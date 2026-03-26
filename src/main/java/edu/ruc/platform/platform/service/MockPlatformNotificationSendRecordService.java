package edu.ruc.platform.platform.service;

import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.platform.dto.PlatformNotificationSendRecordResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
public class MockPlatformNotificationSendRecordService implements PlatformNotificationSendRecordService {

    private final AtomicLong idGenerator = new AtomicLong(2);
    private final List<PlatformNotificationSendRecordResponse> records = new ArrayList<>(List.of(
            new PlatformNotificationSendRecordResponse(1L, "奖学金材料提交通知", "IN_APP", "GRADE", "2023级", "SENT", 320, "系统管理员", LocalDateTime.of(2026, 3, 23, 10, 0), List.of("EMAIL", "WECHAT")),
            new PlatformNotificationSendRecordResponse(2L, "证明审批结果通知", "IN_APP", "SELF", "studentId=10001", "SENT", 1, "胡浩老师", LocalDateTime.of(2026, 3, 23, 15, 30), List.of("EMAIL", "WECHAT"))
    ));

    @Override
    public List<PlatformNotificationSendRecordResponse> listRecords() {
        return records.stream().toList();
    }

    @Override
    public PageResponse<PlatformNotificationSendRecordResponse> pageRecords(String channel, String status, String targetKeyword, int page, int size) {
        List<PlatformNotificationSendRecordResponse> filtered = listRecords().stream()
                .filter(item -> channel == null || channel.isBlank() || channel.equals(item.channel()))
                .filter(item -> status == null || status.isBlank() || status.equals(item.status()))
                .filter(item -> targetKeyword == null || targetKeyword.isBlank() || item.targetDescription().contains(targetKeyword))
                .toList();
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public PlatformNotificationSendRecordResponse recordSend(String title,
                                                            String channel,
                                                            String targetType,
                                                            String targetDescription,
                                                            String status,
                                                            Integer recipientCount,
                                                            String triggeredBy,
                                                            LocalDateTime sentAt,
                                                            List<String> extensionChannels) {
        PlatformNotificationSendRecordResponse response = new PlatformNotificationSendRecordResponse(
                idGenerator.incrementAndGet(),
                title,
                channel,
                targetType,
                targetDescription,
                status,
                recipientCount,
                triggeredBy,
                sentAt == null ? LocalDateTime.now() : sentAt,
                extensionChannels == null ? List.of() : extensionChannels
        );
        records.add(0, response);
        return response;
    }
}
