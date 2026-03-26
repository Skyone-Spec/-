package edu.ruc.platform.platform.service;

import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.platform.domain.PlatformNotificationSendRecord;
import edu.ruc.platform.platform.dto.PlatformNotificationSendRecordResponse;
import edu.ruc.platform.platform.repository.PlatformNotificationSendRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class DefaultPlatformNotificationSendRecordService implements PlatformNotificationSendRecordService {

    private final PlatformNotificationSendRecordRepository platformNotificationSendRecordRepository;

    @Override
    public List<PlatformNotificationSendRecordResponse> listRecords() {
        return platformNotificationSendRecordRepository.findAllByOrderBySentAtDescCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PageResponse<PlatformNotificationSendRecordResponse> pageRecords(String channel, String status, String targetKeyword, int page, int size) {
        List<PlatformNotificationSendRecordResponse> filtered = listRecords().stream()
                .filter(item -> channel == null || channel.isBlank() || channel.equals(item.channel()))
                .filter(item -> status == null || status.isBlank() || status.equals(item.status()))
                .filter(item -> targetKeyword == null || targetKeyword.isBlank() || item.targetDescription().contains(targetKeyword))
                .toList();
        return toPage(filtered, page, size);
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
        PlatformNotificationSendRecord record = new PlatformNotificationSendRecord();
        record.setTitle(title);
        record.setChannel(channel);
        record.setTargetType(targetType);
        record.setTargetDescription(targetDescription);
        record.setStatus(status);
        record.setRecipientCount(recipientCount);
        record.setTriggeredBy(triggeredBy);
        record.setSentAt(sentAt == null ? LocalDateTime.now() : sentAt);
        record.setExtensionChannels(extensionChannels == null || extensionChannels.isEmpty()
                ? null
                : String.join(",", extensionChannels));
        return toResponse(platformNotificationSendRecordRepository.save(record));
    }

    private PlatformNotificationSendRecordResponse toResponse(PlatformNotificationSendRecord item) {
        return new PlatformNotificationSendRecordResponse(
                item.getId(),
                item.getTitle(),
                item.getChannel(),
                item.getTargetType(),
                item.getTargetDescription(),
                item.getStatus(),
                item.getRecipientCount(),
                item.getTriggeredBy(),
                item.getSentAt(),
                item.getExtensionChannels() == null || item.getExtensionChannels().isBlank()
                        ? List.of()
                        : Arrays.stream(item.getExtensionChannels().split(","))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .toList()
        );
    }

    private <T> PageResponse<T> toPage(List<T> items, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, items.size());
        int toIndex = Math.min(fromIndex + normalizedSize, items.size());
        int totalPages = (int) Math.ceil(items.size() / (double) normalizedSize);
        return new PageResponse<>(items.subList(fromIndex, toIndex), items.size(), totalPages, normalizedPage, normalizedSize);
    }
}
