package edu.ruc.platform.platform.service;

import edu.ruc.platform.auth.domain.UserAccount;
import edu.ruc.platform.auth.repository.UserAccountRepository;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.dto.BatchImportResultResponse;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class ExcelImportExportServiceImpl implements ExcelImportExportService {

    private final UserAccountRepository userAccountRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public BatchImportResultResponse importUsers(MultipartFile file) {
        List<BatchImportResultResponse.ImportErrorItem> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows = 0;
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new BusinessException("Excel文件为空");
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                totalRows++;
                try {
                    String username = getCellStringValue(row, 0);
                    String name = getCellStringValue(row, 1);
                    String role = getCellStringValue(row, 2);
                    String rawPassword = getCellStringValue(row, 3);
                    
                    if (username == null || username.isBlank()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "username", "用户名不能为空", ""));
                        continue;
                    }
                    
                    if (userAccountRepository.findByUsername(username).isPresent()) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "username", "用户名已存在: " + username, username));
                        continue;
                    }
                    
                    RoleType roleType;
                    try {
                        roleType = RoleType.valueOf(role.toUpperCase());
                    } catch (Exception e) {
                        errors.add(new BatchImportResultResponse.ImportErrorItem(i + 1, "role", "角色不存在: " + role, role));
                        continue;
                    }
                    
                    UserAccount account = new UserAccount();
                    account.setUsername(username);
                    account.setPasswordHash(passwordEncoder.encode(rawPassword != null && !rawPassword.isBlank() ? rawPassword : "123456"));
                    account.setRole(roleType);
                    account.setEnabled(true);
                    account.setPasswordResetRequired(true);
                    userAccountRepository.save(account);
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
    public byte[] exportUsers(String role, Boolean enabled) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户列表");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("用户ID");
            headerRow.createCell(1).setCellValue("用户名");
            headerRow.createCell(2).setCellValue("角色");
            headerRow.createCell(3).setCellValue("启用状态");
            headerRow.createCell(4).setCellValue("学号");
            headerRow.createCell(5).setCellValue("姓名");
            
            List<UserAccount> users = userAccountRepository.findAll();
            int rowNum = 1;
            for (UserAccount user : users) {
                if (role != null && !role.equalsIgnoreCase(user.getRole().name())) continue;
                if (enabled != null && !enabled.equals(user.getEnabled())) continue;
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getRole().name());
                row.createCell(3).setCellValue(user.getEnabled() ? "启用" : "禁用");
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
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
                if (row == null) continue;
                
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
                    
                    StudentProfile profile = studentProfileRepository.findByStudentNo(studentNo).orElse(null);
                    if (profile == null) {
                        profile = new StudentProfile();
                        profile.setStudentNo(studentNo);
                    }
                    
                    profile.setName(name);
                    profile.setMajor(major);
                    profile.setGrade(grade);
                    profile.setClassName(className);
                    profile.setEmail(email);
                    profile.setStatus("ACTIVE");
                    studentProfileRepository.save(profile);
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
            headerRow.createCell(2).setCellValue("专业");
            headerRow.createCell(3).setCellValue("年级");
            headerRow.createCell(4).setCellValue("班级");
            headerRow.createCell(5).setCellValue("邮箱");
            headerRow.createCell(6).setCellValue("状态");
            
            List<StudentProfile> students = studentProfileRepository.findAll();
            int rowNum = 1;
            for (StudentProfile student : students) {
                if (grade != null && !grade.equals(student.getGrade())) continue;
                if (className != null && !className.equals(student.getClassName())) continue;
                if (status != null && !status.equals(student.getStatus())) continue;
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getStudentNo());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(student.getMajor());
                row.createCell(3).setCellValue(student.getGrade());
                row.createCell(4).setCellValue(student.getClassName());
                row.createCell(5).setCellValue(student.getEmail());
                row.createCell(6).setCellValue(student.getStatus());
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
    }

    private String getCellStringValue(Row row, int cellNum) {
        Cell cell = row.getCell(cellNum);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}
