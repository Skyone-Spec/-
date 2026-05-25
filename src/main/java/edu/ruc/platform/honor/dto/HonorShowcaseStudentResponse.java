package edu.ruc.platform.honor.dto;

import java.util.List;

public record HonorShowcaseStudentResponse(
        Long id,
        Integer awardYear,
        String honorCategory,
        String recipientType,
        String title,
        String description,
        long recipientCount,
        List<HonorRecipientStudentResponse> recipients
) {
}
