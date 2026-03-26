package edu.ruc.platform.admin.dto;

public record AdminNoticeFilterRequest(
        String keyword,
        String tag,
        String targetKeyword
) {
}
