package edu.ruc.platform.student.service;

import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.dto.StudentProfileFilterRequest;
import edu.ruc.platform.student.dto.StudentProfileStatsResponse;
import edu.ruc.platform.student.dto.StudentProfileUpsertRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryCreateRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryResponse;
import edu.ruc.platform.student.dto.StudentPortraitFilterRequest;
import edu.ruc.platform.student.dto.StudentPortraitPageItemResponse;
import edu.ruc.platform.student.dto.StudentPortraitResponse;
import edu.ruc.platform.student.dto.StudentPortraitStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitUpsertRequest;
import edu.ruc.platform.common.api.PageResponse;

import java.util.List;

public interface StudentProfileApplicationService {

    List<StudentProfileResponse> listStudents();

    List<StudentProfileResponse> listStudentsByScope(String grade, String className);

    StudentProfileResponse getStudent(Long id);

    StudentProfileResponse currentStudentProfile();

    List<StudentProfileResponse> listStudentsByGrade(String grade);

    PageResponse<StudentProfileResponse> pageStudents(StudentProfileFilterRequest request, int page, int size);

    StudentProfileStatsResponse studentStats(StudentProfileFilterRequest request);

    StudentProfileResponse createStudent(StudentProfileUpsertRequest request);

    StudentProfileResponse updateStudent(Long id, StudentProfileUpsertRequest request);

    List<StudentStatusHistoryResponse> listStatusHistory(Long studentId);

    StudentStatusHistoryResponse createStatusHistory(Long studentId, StudentStatusHistoryCreateRequest request);

    StudentPortraitResponse getPortrait(Long studentId);

    StudentPortraitResponse upsertPortrait(Long studentId, StudentPortraitUpsertRequest request);

    PageResponse<StudentPortraitPageItemResponse> pagePortraits(StudentPortraitFilterRequest request, int page, int size);

    StudentPortraitStatsResponse portraitStats(StudentPortraitFilterRequest request);
}
