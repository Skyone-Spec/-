package edu.ruc.platform.platform.service;

import edu.ruc.platform.platform.dto.BatchImportResultResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportExportService {
    BatchImportResultResponse importUsers(MultipartFile file);
    byte[] exportUsers(String role, Boolean enabled);
    BatchImportResultResponse importStudents(MultipartFile file);
    byte[] exportStudents(String grade, String className, String status);
}
