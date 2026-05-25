package edu.ruc.platform.platform.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.dto.BatchImportResultResponse;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.dto.StudentProfileUpsertRequest;
import edu.ruc.platform.student.service.MockStudentGrowthService;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockExcelImportExportService implements ExcelImportExportService {

    private final StudentProfileApplicationService studentProfileService;
    private final MockStudentGrowthService mockStudentGrowthService;

    @Override
    public BatchImportResultResponse importUsers(MultipartFile file) {
        return readWorkbookAndCount(file, 4);
    }

    @Override
    public byte[] exportUsers(String role, Boolean enabled) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户列表");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("用户名");
            headerRow.createCell(1).setCellValue("姓名");
            headerRow.createCell(2).setCellValue("角色");
            headerRow.createCell(3).setCellValue("状态");
            return writeWorkbook(workbook);
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public BatchImportResultResponse importStudents(MultipartFile file) {
        List<BatchImportResultResponse.ImportErrorItem> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new BusinessException("Excel文件为空");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowBlank(row, 6)) continue;
                totalRows++;
                try {
                    String studentNo = getCellStringValue(row, 0);
                    String name = getCellStringValue(row, 1);
                    String major = getCellStringValue(row, 2);
                    String grade = getCellStringValue(row, 3);
                    String className = getCellStringValue(row, 4);
                    String email = getCellStringValue(row, 5);

                    if (studentNo == null || studentNo.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "studentNo", "学号不能为空", ""));
                        continue;
                    }
                    if (name == null || name.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "name", "姓名不能为空", studentNo));
                        continue;
                    }

                    StudentProfileResponse existing = findStudent(studentNo);
                    StudentProfileUpsertRequest request = new StudentProfileUpsertRequest(
                            studentNo,
                            name,
                            existing == null ? null : existing.collegeName(),
                            major,
                            grade,
                            className,
                            existing == null ? null : existing.advisorScope(),
                            existing == null ? "本科" : existing.degreeLevel(),
                            email,
                            Boolean.FALSE,
                            "ACTIVE",
                            null,
                            null,
                            null,
                            null,
                            null
                    );
                    if (existing == null) {
                        studentProfileService.createStudent(request);
                    } else {
                        studentProfileService.updateStudent(existing.id(), request);
                    }
                    successRows++;
                } catch (Exception e) {
                    errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "general", e.getMessage(), ""));
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }

        return new BatchImportResultResponse(totalRows, successRows, totalRows - successRows, errors);
    }

    @Override
    public BatchImportResultResponse importAwardSupportRecords(MultipartFile file) {
        List<BatchImportResultResponse.ImportErrorItem> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new BusinessException("Excel文件为空");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowBlank(row, 8)) continue;
                totalRows++;
                try {
                    String studentNo = getCellStringValue(row, 0);
                    String assessmentAcademicYear = getCellStringValue(row, 1);
                    String awardName = getCellStringValue(row, 2);
                    String batchName = getCellStringValue(row, 3);
                    String awardLevel = getCellStringValue(row, 4);
                    String awardGrade = getCellStringValue(row, 5);
                    String awardAmount = getCellStringValue(row, 6);
                    String awardType = getCellStringValue(row, 7);

                    if (studentNo == null || studentNo.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "studentNo", "学号不能为空", ""));
                        continue;
                    }
                    if (assessmentAcademicYear == null || assessmentAcademicYear.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "assessmentAcademicYear", "评定学年不能为空", studentNo));
                        continue;
                    }
                    if (awardName == null || awardName.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "awardName", "奖学金名称不能为空", studentNo));
                        continue;
                    }

                    StudentProfileResponse student = studentProfileService.getStudentByStudentNo(studentNo);
                    Map<String, Object> fields = new LinkedHashMap<>();
                    fields.put("assessmentAcademicYear", assessmentAcademicYear);
                    fields.put("awardName", awardName);
                    fields.put("batchName", batchName);
                    fields.put("awardLevel", awardLevel);
                    fields.put("awardGrade", awardGrade);
                    fields.put("awardAmount", awardAmount);
                    fields.put("awardType", awardType);
                    mockStudentGrowthService.importAwardSupportRecord(student.id(), fields);
                    successRows++;
                } catch (Exception e) {
                    errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "general", e.getMessage(), ""));
                }
            }
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }

        return new BatchImportResultResponse(totalRows, successRows, totalRows - successRows, errors);
    }

    @Override
    public byte[] exportStudents(String grade, String className, String status) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生列表");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("学号");
            headerRow.createCell(1).setCellValue("姓名");
            headerRow.createCell(2).setCellValue("学院");
            headerRow.createCell(3).setCellValue("专业");
            headerRow.createCell(4).setCellValue("年级");
            headerRow.createCell(5).setCellValue("班级");
            headerRow.createCell(6).setCellValue("邮箱");
            headerRow.createCell(7).setCellValue("状态");

            int rowNum = 1;
            for (StudentProfileResponse item : studentProfileService.listStudents()) {
                if (grade != null && !grade.equals(item.grade())) continue;
                if (className != null && !className.equals(item.className())) continue;
                if (status != null && !status.equals(item.status())) continue;
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.studentNo());
                row.createCell(1).setCellValue(item.name());
                row.createCell(2).setCellValue(item.collegeName() == null ? "" : item.collegeName());
                row.createCell(3).setCellValue(item.major() == null ? "" : item.major());
                row.createCell(4).setCellValue(item.grade() == null ? "" : item.grade());
                row.createCell(5).setCellValue(item.className() == null ? "" : item.className());
                row.createCell(6).setCellValue(item.email() == null ? "" : item.email());
                row.createCell(7).setCellValue(item.status() == null ? "" : item.status());
            }
            return writeWorkbook(workbook);
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    private BatchImportResultResponse readWorkbookAndCount(MultipartFile file, int cellCount) {
        List<BatchImportResultResponse.ImportErrorItem> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new BusinessException("Excel文件为空");
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowBlank(row, cellCount)) continue;
                totalRows++;
                successRows++;
            }
            return new BatchImportResultResponse(totalRows, successRows, 0, errors);
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
    }

    private StudentProfileResponse findStudent(String studentNo) {
        try {
            return studentProfileService.getStudentByStudentNo(studentNo);
        } catch (BusinessException ex) {
            return null;
        }
    }

    private byte[] writeWorkbook(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }

    private String getCellStringValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private boolean isRowBlank(Row row, int cellCount) {
        for (int i = 0; i < cellCount; i++) {
            String value = getCellStringValue(row, i);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
