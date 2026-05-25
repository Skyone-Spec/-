package edu.ruc.platform.student.service;

import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthArchiveResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthModuleResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthRecordRequest;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthRecordResponse;

import java.util.List;

public interface StudentGrowthApplicationService {

    List<StudentGrowthModuleResponse> listModules();

    StudentGrowthArchiveResponse currentArchive();

    StudentGrowthArchiveResponse archiveByStudentId(Long studentId);

    List<StudentGrowthRecordResponse> listRecords(String moduleCode);

    StudentGrowthRecordResponse getRecord(String moduleCode, Long id);

    StudentGrowthRecordResponse createRecord(String moduleCode, StudentGrowthRecordRequest request);

    StudentGrowthRecordResponse updateRecord(String moduleCode, Long id, StudentGrowthRecordRequest request);

    void deleteRecord(String moduleCode, Long id);
}
