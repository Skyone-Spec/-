package edu.ruc.platform.notice.dto;

import java.time.LocalDateTime;

public record NoticeResponse(Long id, String title, String summary, String tag, LocalDateTime publishTime) {
}
