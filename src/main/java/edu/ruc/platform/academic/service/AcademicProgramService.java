package edu.ruc.platform.academic.service;

import edu.ruc.platform.academic.domain.*;
import edu.ruc.platform.academic.dto.*;
import edu.ruc.platform.academic.repository.*;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class AcademicProgramService implements AcademicProgramApplicationService {

    private final AcademicProgramRepository programRepository;
    private final AcademicProgramModuleRepository moduleRepository;
    private final AcademicTranscriptRepository transcriptRepository;
    private final AcademicTranscriptItemRepository transcriptItemRepository;
    private final StudentProfileRepository studentProfileRepository;

    @Override
    public List<AcademicProgramResponse> listAll() {
        return programRepository.findAll().stream()
                .map(program -> toResponse(program, true))
                .toList();
    }

    @Override
    public AcademicProgramResponse getById(Long id) {
        AcademicProgram program = programRepository.findById(id)
                .orElseThrow(() -> new BusinessException("培养方案不存在"));
        return toResponse(program, true);
    }

    @Override
    public AcademicProgramResponse create(AcademicProgramCreateRequest request) {
        if (programRepository.findByProgramCode(request.programCode()).isPresent()) {
            throw new BusinessException("培养方案编码已存在: " + request.programCode());
        }
        AcademicProgram program = new AcademicProgram();
        program.setProgramCode(request.programCode());
        program.setProgramName(request.programName());
        program.setMajor(request.major());
        program.setGrade(request.grade());
        program.setTotalCredits(request.totalCredits());
        program.setDescription(request.description());
        program.setIsActive(true);
        return toResponse(programRepository.save(program), false);
    }

    @Override
    public AcademicProgramResponse update(Long id, AcademicProgramCreateRequest request) {
        AcademicProgram program = programRepository.findById(id)
                .orElseThrow(() -> new BusinessException("培养方案不存在"));
        program.setProgramName(request.programName());
        program.setMajor(request.major());
        program.setGrade(request.grade());
        program.setTotalCredits(request.totalCredits());
        program.setDescription(request.description());
        return toResponse(programRepository.save(program), true);
    }

    @Override
    public void delete(Long id) {
        if (!programRepository.existsById(id)) {
            throw new BusinessException("培养方案不存在");
        }
        programRepository.deleteById(id);
    }

    @Override
    public AcademicProgramModuleResponse addModule(Long programId, AcademicProgramModuleCreateRequest request) {
        if (!programRepository.existsById(programId)) {
            throw new BusinessException("培养方案不存在");
        }
        AcademicProgramModule module = new AcademicProgramModule();
        module.setProgramId(programId);
        module.setModuleCode(request.moduleCode());
        module.setModuleName(request.moduleName());
        module.setModuleType(request.moduleType());
        module.setRequiredCredits(request.requiredCredits());
        module.setDescription(request.description());
        module.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        module = moduleRepository.save(module);
        return toModuleResponse(module);
    }

    @Override
    public void removeModule(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new BusinessException("模块不存在");
        }
        moduleRepository.deleteById(moduleId);
    }

    @Override
    public TranscriptUploadResponse uploadTranscript(Long studentId, MultipartFile file) {
        if (!studentProfileRepository.existsById(studentId)) {
            throw new BusinessException("学生不存在");
        }

        List<TranscriptUploadResponse.TranscriptItemResponse> items = new ArrayList<>();
        int totalCourses = 0;
        int passedCourses = 0;
        double totalCredits = 0;
        double totalGradePoints = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new BusinessException("Excel文件为空");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String courseCode = getCellStringValue(row, 0);
                String courseName = getCellStringValue(row, 1);
                double credits = getCellNumericValue(row, 2);
                String gradeText = getCellStringValue(row, 3);
                double gradePoint = getCellNumericValue(row, 4);
                boolean passed = gradePoint >= 1.0;

                items.add(new TranscriptUploadResponse.TranscriptItemResponse(
                        courseCode, courseName, credits, gradeText, gradePoint, passed
                ));

                totalCourses++;
                if (passed) {
                    passedCourses++;
                    totalCredits += credits;
                }
                totalGradePoints += gradePoint * credits;
            }
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }

        AcademicTranscript transcript = new AcademicTranscript();
        transcript.setStudentId(studentId);
        transcript.setTerm(LocalDateTime.now().getYear() + "-" + (LocalDateTime.now().getMonthValue() > 6 ? "2" : "1"));
        transcript.setGpa(totalCredits > 0 ? totalGradePoints / totalCredits : 0);
        transcript.setTotalCredits(totalCredits);
        transcript.setTotalCourses(totalCourses);
        transcript.setPassedCourses(passedCourses);
        transcript.setParsedAt(LocalDateTime.now());
        transcript = transcriptRepository.save(transcript);

        for (TranscriptUploadResponse.TranscriptItemResponse item : items) {
            AcademicTranscriptItem entity = new AcademicTranscriptItem();
            entity.setTranscriptId(transcript.getId());
            entity.setTerm(transcript.getTerm());
            entity.setCourseCode(item.courseCode());
            entity.setCourseName(item.courseName());
            entity.setCredits(item.credits());
            entity.setGradeText(item.gradeText());
            entity.setGradePoint(item.gradePoint());
            entity.setPassed(item.passed());
            transcriptItemRepository.save(entity);
        }

        return new TranscriptUploadResponse(
                transcript.getId(), studentId, transcript.getTerm(),
                transcript.getGpa(), transcript.getTotalCredits(),
                transcript.getTotalCourses(), transcript.getPassedCourses(),
                totalCourses - passedCourses, items, transcript.getParsedAt()
        );
    }

    @Override
    public TranscriptUploadResponse getTranscript(Long studentId) {
        AcademicTranscript transcript = transcriptRepository.findFirstByStudentIdOrderByParsedAtDesc(studentId)
                .orElseThrow(() -> new BusinessException("未找到成绩单"));
        List<AcademicTranscriptItem> items = transcriptItemRepository.findByTranscriptId(transcript.getId());
        return new TranscriptUploadResponse(
                transcript.getId(), studentId, transcript.getTerm(),
                transcript.getGpa(), transcript.getTotalCredits(),
                transcript.getTotalCourses(), transcript.getPassedCourses(),
                transcript.getTotalCourses() - transcript.getPassedCourses(),
                items.stream().map(item -> new TranscriptUploadResponse.TranscriptItemResponse(
                        item.getCourseCode(), item.getCourseName(), item.getCredits(),
                        item.getGradeText(), item.getGradePoint(), item.getPassed()
                )).toList(),
                transcript.getParsedAt()
        );
    }

    @Override
    public AuditReportResponse generateAuditReport(Long studentId, Long programId) {
        StudentProfile student = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));
        AcademicProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException("培养方案不存在"));

        List<AcademicProgramModule> modules = moduleRepository.findByProgramIdOrderBySortOrder(programId);
        AcademicTranscript transcript = transcriptRepository.findFirstByStudentIdOrderByParsedAtDesc(studentId).orElse(null);

        int totalRequiredCredits = program.getTotalCredits();
        int earnedCredits = transcript != null ? transcript.getTotalCredits().intValue() : 0;
        List<AuditReportResponse.MissingModuleResponse> missingModules = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        for (AcademicProgramModule module : modules) {
            int moduleEarned = 0;
            if (transcript != null) {
                List<AcademicTranscriptItem> passedItems = transcriptItemRepository.findByTranscriptId(transcript.getId())
                        .stream().filter(AcademicTranscriptItem::getPassed).toList();
                moduleEarned = passedItems.stream()
                        .mapToInt(item -> item.getCredits().intValue())
                        .sum();
            }
            int missing = Math.max(0, module.getRequiredCredits() - moduleEarned);
            if (missing > 0) {
                missingModules.add(new AuditReportResponse.MissingModuleResponse(
                        module.getModuleName(), module.getModuleType(),
                        module.getRequiredCredits(), moduleEarned, missing
                ));
                recommendations.add("建议选修" + module.getModuleName() + "相关课程，还需" + missing + "学分");
            }
        }

        int missingCredits = Math.max(0, totalRequiredCredits - earnedCredits);
        double completionRate = totalRequiredCredits > 0 ? (double) earnedCredits / totalRequiredCredits * 100 : 0;

        return new AuditReportResponse(
                studentId, student.getName(), program.getProgramName(),
                totalRequiredCredits, earnedCredits, missingCredits,
                completionRate, missingModules, recommendations
        );
    }

    private String getCellStringValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private double getCellNumericValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return 0;
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private AcademicProgramResponse toResponse(AcademicProgram program, boolean includeModules) {
        List<AcademicProgramModuleResponse> modules = includeModules ?
                moduleRepository.findByProgramIdOrderBySortOrder(program.getId())
                        .stream().map(this::toModuleResponse).toList() :
                List.of();
        return new AcademicProgramResponse(
                program.getId(), program.getProgramCode(), program.getProgramName(),
                program.getMajor(), program.getGrade(), program.getTotalCredits(),
                program.getDescription(), program.getIsActive(), modules, program.getCreatedAt()
        );
    }

    private AcademicProgramModuleResponse toModuleResponse(AcademicProgramModule module) {
        return new AcademicProgramModuleResponse(
                module.getId(), module.getModuleCode(), module.getModuleName(),
                module.getModuleType(), module.getRequiredCredits(),
                module.getDescription(), module.getSortOrder()
        );
    }
}
