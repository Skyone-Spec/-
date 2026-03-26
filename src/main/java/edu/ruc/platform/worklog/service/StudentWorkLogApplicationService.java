package edu.ruc.platform.worklog.service;

import edu.ruc.platform.worklog.dto.StudentWorkLogCreateRequest;
import edu.ruc.platform.worklog.dto.StudentWorkLogResponse;
import edu.ruc.platform.worklog.dto.StudentWorkLogUpdateRequest;
import edu.ruc.platform.worklog.dto.StudentWorkloadSummaryResponse;
import edu.ruc.platform.worklog.dto.WorklogAdminStatsResponse;
import edu.ruc.platform.worklog.dto.WorklogActionLogResponse;
import edu.ruc.platform.worklog.dto.WorklogExportMetadataResponse;
import edu.ruc.platform.worklog.dto.WorklogFilterRequest;
import edu.ruc.platform.worklog.dto.WorklogOverviewResponse;
import edu.ruc.platform.common.api.PageResponse;

import java.util.List;

public interface StudentWorkLogApplicationService {

    StudentWorkLogResponse create(StudentWorkLogCreateRequest request);

    StudentWorkLogResponse update(Long id, StudentWorkLogUpdateRequest request);

    void delete(Long id);

    List<StudentWorkLogResponse> listByStudent(Long studentId);

    List<WorklogActionLogResponse> listActionLogs(Long id);

    StudentWorkloadSummaryResponse summarize(Long studentId);

    WorklogOverviewResponse overview();

    List<StudentWorkLogResponse> filter(WorklogFilterRequest request, String sortBy, String sortDir);

    WorklogAdminStatsResponse adminStats(WorklogFilterRequest request);

    PageResponse<StudentWorkLogResponse> adminPage(WorklogFilterRequest request, int page, int size, String sortBy, String sortDir);

    WorklogExportMetadataResponse exportMetadata();
}
