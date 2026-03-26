package edu.ruc.platform.academic.service;

import edu.ruc.platform.academic.dto.AcademicAnalysisResponse;

public interface AcademicWarningApplicationService {

    AcademicAnalysisResponse analyze(Long studentId);
}
