package edu.ruc.platform.honor.dto;

public record HonorShowcaseFilterRequest(
        Integer awardYear,
        String honorCategory,
        String recipientType,
        Boolean publicVisible,
        String keyword
) {
}
