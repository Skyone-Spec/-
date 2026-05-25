package edu.ruc.platform.student.controller;

import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthArchiveResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthModuleResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthRecordRequest;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthRecordResponse;
import edu.ruc.platform.student.service.StudentGrowthApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/student/growth")
@RequiredArgsConstructor
public class StudentGrowthController {

    private final StudentGrowthApplicationService studentGrowthService;

    @GetMapping("/modules")
    public ApiResponse<List<StudentGrowthModuleResponse>> modules() {
        return ApiResponse.success(studentGrowthService.listModules());
    }

    @GetMapping("/archive")
    public ApiResponse<StudentGrowthArchiveResponse> archive() {
        return ApiResponse.success(studentGrowthService.currentArchive());
    }

    @GetMapping("/{moduleCode}/records")
    public ApiResponse<List<StudentGrowthRecordResponse>> listRecords(@PathVariable String moduleCode) {
        return ApiResponse.success(studentGrowthService.listRecords(moduleCode));
    }

    @GetMapping("/{moduleCode}/records/{id}")
    public ApiResponse<StudentGrowthRecordResponse> detail(@PathVariable String moduleCode,
                                                           @Positive(message = "记录ID必须大于 0") @PathVariable Long id) {
        return ApiResponse.success(studentGrowthService.getRecord(moduleCode, id));
    }

    @PostMapping("/{moduleCode}/records")
    public ApiResponse<StudentGrowthRecordResponse> create(@PathVariable String moduleCode,
                                                           @Valid @RequestBody StudentGrowthRecordRequest request) {
        return ApiResponse.success("成长记录已保存", studentGrowthService.createRecord(moduleCode, request));
    }

    @PutMapping("/{moduleCode}/records/{id}")
    public ApiResponse<StudentGrowthRecordResponse> update(@PathVariable String moduleCode,
                                                           @Positive(message = "记录ID必须大于 0") @PathVariable Long id,
                                                           @Valid @RequestBody StudentGrowthRecordRequest request) {
        return ApiResponse.success("成长记录已更新", studentGrowthService.updateRecord(moduleCode, id, request));
    }

    @DeleteMapping("/{moduleCode}/records/{id}")
    public ApiResponse<Void> delete(@PathVariable String moduleCode,
                                    @Positive(message = "记录ID必须大于 0") @PathVariable Long id) {
        studentGrowthService.deleteRecord(moduleCode, id);
        return ApiResponse.success("成长记录已删除", null);
    }
}
