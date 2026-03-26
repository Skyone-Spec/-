package edu.ruc.platform.platform.service;

import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.platform.dto.PlatformNotificationSendRecordResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface PlatformNotificationSendRecordService {

    List<PlatformNotificationSendRecordResponse> listRecords();

    PageResponse<PlatformNotificationSendRecordResponse> pageRecords(String channel, String status, String targetKeyword, int page, int size);

    PlatformNotificationSendRecordResponse recordSend(String title,
                                                      String channel,
                                                      String targetType,
                                                      String targetDescription,
                                                      String status,
                                                      Integer recipientCount,
                                                      String triggeredBy,
                                                      LocalDateTime sentAt,
                                                      List<String> extensionChannels);
}
