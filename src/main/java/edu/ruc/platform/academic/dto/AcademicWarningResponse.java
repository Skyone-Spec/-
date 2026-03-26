package edu.ruc.platform.academic.dto;

public record AcademicWarningResponse(String moduleName,
                                      Integer requiredCredits,
                                      Integer earnedCredits,
                                      Integer missingCredits,
                                      Integer completionRate,
                                      String recommendedCourses) {
}
