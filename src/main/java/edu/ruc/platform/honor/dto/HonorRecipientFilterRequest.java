package edu.ruc.platform.honor.dto;

public record HonorRecipientFilterRequest(
        String recipientType,
        Boolean publicVisible,
        String keyword
) {
}
