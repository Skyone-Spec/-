package edu.ruc.platform.common.enums;

import edu.ruc.platform.common.exception.BusinessException;

public enum DataImportTaskStatus {
    CREATED,
    RUNNING,
    SUCCESS,
    PARTIAL_SUCCESS,
    FAILED;

    public static DataImportTaskStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("导入任务状态不能为空");
        }
        try {
            return DataImportTaskStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("导入任务状态不支持: " + value);
        }
    }
}
