package edu.ruc.platform.student.dto;

public record StudentPortraitFilterRequest(
        String grade,
        String className,
        Boolean publicVisible,
        String careerOrientation,
        Double minGpa
) {
}
