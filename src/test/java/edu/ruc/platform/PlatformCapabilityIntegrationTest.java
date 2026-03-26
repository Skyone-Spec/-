package edu.ruc.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class PlatformCapabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void platformContractsAndStudentQueryUseUnifiedConventions() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(get("/api/v1/platform/contracts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.userIdField").value("userId"))
                .andExpect(jsonPath("$.data.studentIdField").value("studentId"))
                .andExpect(jsonPath("$.data.studentNoField").value("studentNo"))
                .andExpect(jsonPath("$.data.permissionCheckEntry").value("CurrentUserService"))
                .andExpect(jsonPath("$.data.uploadResponseType").value("PlatformFileUploadResponse"))
                .andExpect(jsonPath("$.data.pageRequest").value("page,size"))
                .andExpect(jsonPath("$.data.pageResponse").value("ApiResponse<PageResponse<T>>"))
                .andExpect(jsonPath("$.data.roleEnums[0]").value("STUDENT"))
                .andExpect(jsonPath("$.data.roleEnums[5]").value("COLLEGE_ADMIN"))
                .andExpect(jsonPath("$.data.importTaskStatuses[0]").value("CREATED"))
                .andExpect(jsonPath("$.data.importTaskStatuses[4]").value("FAILED"))
                .andExpect(jsonPath("$.data.notificationChannels[0]").value("IN_APP"))
                .andExpect(jsonPath("$.data.notificationTargetTypes[2]").value("CLASS"))
                .andExpect(jsonPath("$.data.notificationSendStatuses[1]").value("SENT"))
                .andExpect(jsonPath("$.data.studentActionTypes[0]").value("PARTY_REMINDER"))
                .andExpect(jsonPath("$.data.studentActionPriorities[0]").value("HIGH"))
                .andExpect(jsonPath("$.data.studentActionPaths.ACADEMIC").value("/student/academic-analysis"))
                .andExpect(jsonPath("$.data.studentActionPaths.WORKLOG").value("/student/worklogs"))
                .andExpect(jsonPath("$.data.approvalStatuses[0]").value("PENDING"))
                .andExpect(jsonPath("$.data.approvalStatuses[1]").value("COUNSELOR_APPROVED"))
                .andExpect(jsonPath("$.data.approvalLogFields[0]").value("operatorId"))
                .andExpect(jsonPath("$.data.approvalLogFields[4]").value("fromStatus"))
                .andExpect(jsonPath("$.data.operationLogFields[0]").value("operatorId"))
                .andExpect(jsonPath("$.data.operationLogFields[3]").value("module"));

        mockMvc.perform(get("/api/v1/platform/student-ui-contract")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.actionTypes[0]").value("PARTY_REMINDER"))
                .andExpect(jsonPath("$.data.actionPriorities[0]").value("HIGH"))
                .andExpect(jsonPath("$.data.actions[2].type").value("ACADEMIC"))
                .andExpect(jsonPath("$.data.actions[2].defaultPriority").value("HIGH"))
                .andExpect(jsonPath("$.data.actions[2].actionPath").value("/student/academic-analysis"))
                .andExpect(jsonPath("$.data.actions[2].iconKey").value("academic-warning"))
                .andExpect(jsonPath("$.data.actions[2].colorKey").value("red"));

        mockMvc.perform(get("/api/v1/platform/users/me/permissions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissionEntries").isArray())
                .andExpect(jsonPath("$.data.dataScopes[0]").value("ALL"));

        mockMvc.perform(get("/api/v1/platform/users/me/student-scope")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allAccess").value(true))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"));

        mockMvc.perform(get("/api/v1/platform/users/me/student-scope/check-student")
                        .param("studentId", "10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true))
                .andExpect(jsonPath("$.data.checkType").value("STUDENT"));

        String advisorToken = loginAndExtractToken("advisor01", "123456");
        mockMvc.perform(get("/api/v1/platform/users/me/student-scope/check-range")
                        .param("grade", "2023级")
                        .param("className", "计科一班")
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(true))
                .andExpect(jsonPath("$.data.className").value("计科一班"));

        mockMvc.perform(get("/api/v1/platform/users/me/student-scope/check-range")
                        .param("grade", "2023级")
                        .param("className", "计科二班")
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowed").value(false))
                .andExpect(jsonPath("$.data.reason").isString());

        mockMvc.perform(get("/api/v1/platform/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role").isString())
                .andExpect(jsonPath("$.data[0].dataScopes").isArray());

        mockMvc.perform(get("/api/v1/platform/security-policy")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maxFailedLoginAttempts").isNumber())
                .andExpect(jsonPath("$.data.defaultPassword").isString());

        mockMvc.perform(get("/api/v1/platform/upload-policy")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maxFileSizeBytes").isNumber())
                .andExpect(jsonPath("$.data.allowedContentTypes").isArray());

        mockMvc.perform(get("/api/v1/platform/users")
                        .param("role", "STUDENT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role").value("STUDENT"))
                .andExpect(jsonPath("$.data[0].userId").isNumber());

        mockMvc.perform(get("/api/v1/platform/users/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$.data.passwordResetRequired").isBoolean())
                .andExpect(jsonPath("$.data.failedLoginAttempts").isNumber());

        mockMvc.perform(get("/api/v1/platform/users/page")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(2));

        mockMvc.perform(get("/api/v1/platform/users/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").isNumber())
                .andExpect(jsonPath("$.data.roleStats").isArray());

        mockMvc.perform(get("/api/v1/platform/sessions/page")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));

        mockMvc.perform(get("/api/v1/platform/students/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(10001))
                .andExpect(jsonPath("$.data.studentNo").value("2023100001"))
                .andExpect(jsonPath("$.data.sensitiveFields.maskedPhone").isString());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void platformUploadAndAuditEndpointsWork() throws Exception {
        String adminToken = loginAndExtractToken("admin", "123456");
        MockMultipartFile file = new MockMultipartFile("file", "proof.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf".getBytes());

        mockMvc.perform(post("/api/v1/platform/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "platform-user-01",
                                  "role": "COUNSELOR",
                                  "enabled": true,
                                  "rawPassword": "123456",
                                  "passwordResetRequired": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("platform-user-01"))
                .andExpect(jsonPath("$.data.role").value("COUNSELOR"))
                .andExpect(jsonPath("$.data.passwordResetRequired").value(true));

        mockMvc.perform(put("/api/v1/platform/security-policy")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "maxFailedLoginAttempts": 6,
                                  "lockDurationMinutes": 45,
                                  "defaultPassword": "888888",
                                  "requirePasswordResetOnCreate": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maxFailedLoginAttempts").value(6))
                .andExpect(jsonPath("$.data.lockDurationMinutes").value(45));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "888888"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"));

        mockMvc.perform(put("/api/v1/platform/users/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "role": "SUPER_ADMIN",
                                  "enabled": true,
                                  "rawPassword": "",
                                  "passwordResetRequired": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"));

        mockMvc.perform(post("/api/v1/platform/users/1/enabled")
                        .param("enabled", "false")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.enabled").value(false));

        mockMvc.perform(post("/api/v1/platform/users/1/unlock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.locked").value(false))
                .andExpect(jsonPath("$.data.failedLoginAttempts").value(0));

        mockMvc.perform(post("/api/v1/platform/users/1/reset-password")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newPassword": "654321"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.temporaryPassword").value("654321"));

        mockMvc.perform(multipart("/api/v1/platform/files/upload")
                        .file(file)
                        .param("bizType", "KNOWLEDGE_ATTACHMENT")
                        .param("bizId", "101")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("文件上传成功"))
                .andExpect(jsonPath("$.data.bizType").value("KNOWLEDGE_ATTACHMENT"))
                .andExpect(jsonPath("$.data.bizId").value(101))
                .andExpect(jsonPath("$.data.fileName").value("proof.pdf"))
                .andExpect(jsonPath("$.data.uploadedBy").isString())
                .andExpect(jsonPath("$.data.uploadedAt").isString());

        mockMvc.perform(put("/api/v1/platform/upload-policy")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "maxFileSizeBytes": 1048576,
                                  "allowedContentTypes": ["application/pdf"],
                                  "allowEmptyContentType": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maxFileSizeBytes").value(1048576))
                .andExpect(jsonPath("$.data.allowedContentTypes[0]").value("application/pdf"))
                .andExpect(jsonPath("$.data.allowEmptyContentType").value(false));

        MockMultipartFile invalidFile = new MockMultipartFile("file", "proof.png", MediaType.IMAGE_PNG_VALUE, "png".getBytes());
        mockMvc.perform(multipart("/api/v1/platform/files/upload")
                        .file(invalidFile)
                        .param("bizType", "KNOWLEDGE_ATTACHMENT")
                        .param("bizId", "101")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("上传文件类型不在平台允许范围内"));

        mockMvc.perform(put("/api/v1/platform/upload-policy")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "maxFileSizeBytes": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("上传大小限制必须大于 0"));

        mockMvc.perform(get("/api/v1/platform/files/page")
                        .param("bizType", "KNOWLEDGE_ATTACHMENT")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].uploadedById").isNumber())
                .andExpect(jsonPath("$.data.content[0].uploadedBy").isString())
                .andExpect(jsonPath("$.data.content[0].archived").value(false))
                .andExpect(jsonPath("$.data.content[0].deleted").value(false))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(1));

        mockMvc.perform(post("/api/v1/platform/files/1001/archive")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.archived").value(true));

        mockMvc.perform(get("/api/v1/platform/audit/login-logs/page")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(post("/api/v1/platform/sessions/8001/revoke")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(8001))
                .andExpect(jsonPath("$.data.active").value(false));

        mockMvc.perform(get("/api/v1/platform/import-tasks/page")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].fileType").isString())
                .andExpect(jsonPath("$.data.content[0].templateName").isString())
                .andExpect(jsonPath("$.data.content[0].progressPercent").isNumber())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(1));

        mockMvc.perform(post("/api/v1/platform/import-tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskType": "STUDENT_PROFILE",
                                  "fileName": "students-import.xlsx",
                                  "totalRows": 12
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskType").value("STUDENT_PROFILE"))
                .andExpect(jsonPath("$.data.fileType").value("xlsx"))
                .andExpect(jsonPath("$.data.templateName").value("学生档案导入模板"))
                .andExpect(jsonPath("$.data.templateDownloadUrl").value("/templates/import/student-profile.xlsx"))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.progressPercent").value(0))
                .andExpect(jsonPath("$.data.owner").value("系统管理员"))
                .andExpect(jsonPath("$.data.errorCount").value(0))
                .andExpect(jsonPath("$.data.recentErrors").isArray())
                .andExpect(jsonPath("$.data.acceptedFileExtensions[0]").value("xlsx"))
                .andExpect(jsonPath("$.data.nextAction").value("上传文件并启动导入"))
                .andExpect(jsonPath("$.data.canRetry").value(false))
                .andExpect(jsonPath("$.data.maintenanceOwner").value("系统管理员"))
                .andExpect(jsonPath("$.data.currentUserCanMaintain").value(true))
                .andExpect(jsonPath("$.data.ownerOnlyMaintenance").value(true))
                .andExpect(jsonPath("$.data.pendingErrorResolution").value(false))
                .andExpect(jsonPath("$.data.receiptCode").isString());

        mockMvc.perform(put("/api/v1/platform/import-tasks/2")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DONE",
                                  "successRows": 26,
                                  "failedRows": 2,
                                  "errorSummary": "invalid status"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("导入任务状态仅支持 CREATED、RUNNING、SUCCESS、PARTIAL_SUCCESS、FAILED"));

        mockMvc.perform(put("/api/v1/platform/import-tasks/2")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PARTIAL_SUCCESS",
                                  "successRows": 26,
                                  "failedRows": 2,
                                  "errorSummary": "2 行数据校验失败"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(2))
                .andExpect(jsonPath("$.data.status").value("PARTIAL_SUCCESS"))
                .andExpect(jsonPath("$.data.progressPercent").value(100))
                .andExpect(jsonPath("$.data.nextAction").value("下载错误明细并按模板修正后重试"))
                .andExpect(jsonPath("$.data.canRetry").value(true))
                .andExpect(jsonPath("$.data.errorCount").value(2))
                .andExpect(jsonPath("$.data.pendingErrorResolution").value(true));

        mockMvc.perform(put("/api/v1/platform/import-tasks/2")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DONE",
                                  "successRows": 26,
                                  "failedRows": 2,
                                  "errorSummary": "invalid status"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("导入任务状态仅支持 CREATED、RUNNING、SUCCESS、PARTIAL_SUCCESS、FAILED"));

        mockMvc.perform(get("/api/v1/platform/import-tasks/2/errors/page")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));

        mockMvc.perform(post("/api/v1/platform/import-tasks/2/errors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rowNumber": 15,
                                  "fieldName": "phone",
                                  "errorMessage": "手机号格式不合法",
                                  "rawValue": "123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(2))
                .andExpect(jsonPath("$.data.templateDownloadUrl").value("/templates/import/knowledge-base.xlsx"))
                .andExpect(jsonPath("$.data.errorCount").value(3))
                .andExpect(jsonPath("$.data.recentErrors").isArray());

        mockMvc.perform(get("/api/v1/platform/import-tasks/2/receipt")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(2))
                .andExpect(jsonPath("$.data.errorCount").value(3))
                .andExpect(jsonPath("$.data.recentErrors").isArray())
                .andExpect(jsonPath("$.data.currentUserCanMaintain").value(true))
                .andExpect(jsonPath("$.data.ownerOnlyMaintenance").value(true))
                .andExpect(jsonPath("$.data.pendingErrorResolution").value(true));

        mockMvc.perform(get("/api/v1/platform/audit/admin-operation-logs/page")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].operatorId").isNumber())
                .andExpect(jsonPath("$.data.content[0].module").isString())
                .andExpect(jsonPath("$.data.content[0].action").isString())
                .andExpect(jsonPath("$.data.content[0].detail").isString());

        mockMvc.perform(get("/api/v1/platform/audit/approval-logs/1002")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].operatorName").isString())
                .andExpect(jsonPath("$.data[0].operatorRole").isString())
                .andExpect(jsonPath("$.data[0].action").isString())
                .andExpect(jsonPath("$.data[0].fromStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[0].toStatus").value("COUNSELOR_APPROVED"))
                .andExpect(jsonPath("$.data[0].operatedAt").isString());

        mockMvc.perform(get("/api/v1/platform/notifications/send-records")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].channel").value("IN_APP"))
                .andExpect(jsonPath("$.data[0].extensionChannels").isArray());

        mockMvc.perform(post("/api/v1/platform/notifications/send")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "班级补录提醒",
                                  "channel": "IN_APP",
                                  "targetType": "CLASS",
                                  "targetDescription": "2023级/计科一班",
                                  "status": "SENT",
                                  "recipientCount": 38,
                                  "extensionChannels": ["EMAIL"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("班级补录提醒"))
                .andExpect(jsonPath("$.data.channel").value("IN_APP"))
                .andExpect(jsonPath("$.data.targetType").value("CLASS"))
                .andExpect(jsonPath("$.data.recipientCount").value(38))
                .andExpect(jsonPath("$.data.triggeredBy").isString())
                .andExpect(jsonPath("$.data.extensionChannels[0]").value("EMAIL"));

        mockMvc.perform(post("/api/v1/platform/notifications/send")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "错误渠道测试",
                                  "channel": "SMS",
                                  "targetType": "CLASS",
                                  "targetDescription": "2023级/计科一班",
                                  "status": "SENT",
                                  "recipientCount": 38
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("发送渠道仅支持 IN_APP、EMAIL、WECHAT"));

        mockMvc.perform(get("/api/v1/platform/notifications/send-records/page")
                        .param("channel", "IN_APP")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].targetType").isString())
                .andExpect(jsonPath("$.data.content[0].status").value("SENT"))
                .andExpect(jsonPath("$.data.content[0].extensionChannels").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    void roleAnnotationBlocksInsufficientPrivilege() throws Exception {
        String token = loginAndExtractToken("2023100001", "123456");

        mockMvc.perform(get("/api/v1/platform/audit/admin-operation-logs/page")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void counselorCanOnlyMaintainOwnImportTask() throws Exception {
        String counselorToken = loginAndExtractToken("teacher01", "123456");

        mockMvc.perform(get("/api/v1/platform/import-tasks/1/receipt")
                        .header("Authorization", "Bearer " + counselorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(1))
                .andExpect(jsonPath("$.data.maintenanceOwner").value("系统管理员"))
                .andExpect(jsonPath("$.data.currentUserCanMaintain").value(false))
                .andExpect(jsonPath("$.data.ownerOnlyMaintenance").value(true));

        mockMvc.perform(put("/api/v1/platform/import-tasks/1")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RUNNING",
                                  "successRows": 10,
                                  "failedRows": 1,
                                  "errorSummary": "尝试越权维护"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("当前账号仅可维护本人创建的导入任务"));
    }

    private String loginAndExtractToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
