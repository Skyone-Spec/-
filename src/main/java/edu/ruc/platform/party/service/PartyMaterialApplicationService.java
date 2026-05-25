package edu.ruc.platform.party.service;

import edu.ruc.platform.party.dto.*;
import java.util.List;

public interface PartyMaterialApplicationService {
    PartyMaterialSubmissionResponse submitMaterial(PartyMaterialSubmitRequest request);
    PartyMaterialSubmissionResponse reviewMaterial(Long submissionId, PartyMaterialReviewRequest request);
    PartyMaterialSubmissionResponse withdrawMaterial(Long submissionId);
    PartyMaterialSubmissionResponse resubmitMaterial(Long submissionId, PartyMaterialSubmitRequest request);
    List<PartyMaterialSubmissionResponse> listByStudentId(Long studentId);
    List<PartyMaterialSubmissionResponse> listPendingByClass(String grade, String className);
    List<PartyActionLogResponse> listActionLogs(Long studentId);
    List<PartyClassProgressResponse> listClassProgress(String grade, String className);
}
