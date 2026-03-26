package edu.ruc.platform.student.dto;

public record StudentProfileFilterRequest(
        String grade,
        String className,
        String status,
        String keyword
) {
}
