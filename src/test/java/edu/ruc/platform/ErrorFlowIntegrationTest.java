package edu.ruc.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class ErrorFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginRejectsWrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void protectedEndpointRejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无效或过期的登录令牌"));
    }

    @Test
    void adminApprovalRejectsUnknownAction() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/admin/approvals/1001/action")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "hold",
                                  "comment": "非法动作"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void resubmitRequiresWithdrawnStatus() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/admin/approvals/1001/action")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "resubmit",
                                  "comment": "重新提交"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void classAdvisorCannotReadOtherStudentsCertificateRequestList() throws Exception {
        String token = loginAndExtractToken("advisor01", "123456");

        mockMvc.perform(get("/api/v1/certificates/requests/student/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("证明申请仅支持学生本人操作"));
    }

    @Test
    void adminCannotCreateOrQueryCertificateRequestForStudent() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/certificates/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 10001,
                                  "certificateType": "在读证明",
                                  "reason": "管理员代提交"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("证明申请仅支持学生本人操作"));

        mockMvc.perform(get("/api/v1/certificates/requests/student/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("证明申请仅支持学生本人操作"));
    }

    @Test
    void classAdvisorCannotReadUnscopedStudentNoticesOrWorklogs() throws Exception {
        String token = loginAndExtractToken("advisor01", "123456");

        mockMvc.perform(get("/api/v1/notices/student/10002")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/v1/worklogs/student/10002")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void classAdvisorCanReadScopedStudentNoticesAndWorklogs() throws Exception {
        String token = loginAndExtractToken("advisor01", "123456");

        mockMvc.perform(get("/api/v1/notices/student/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/worklogs/student/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void studentAdminRejectsInvalidStatusAndPortraitValues() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentNo": "2023999999",
                                  "name": "测试学生",
                                  "major": "计算机类",
                                  "grade": "2023级",
                                  "className": "计科测试班",
                                  "advisorScope": "advisor01|王老师",
                                  "degreeLevel": "本科",
                                  "email": "test@example.edu",
                                  "graduated": false,
                                  "status": "DONE",
                                  "majorChangedTo": null,
                                  "encryptedIdCardNo": "110101199901011234",
                                  "encryptedPhone": "13800001234"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("学生状态仅支持 ACTIVE、SUSPENDED、GRADUATED、TRANSFERRED、WITHDRAWN"));

        mockMvc.perform(post("/api/v1/admin/students/10001/status-history")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "TRANSFERRED",
                                  "changedToMajor": "",
                                  "reason": "测试非法转专业"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("转专业或转出状态必须填写变更后专业"));

        mockMvc.perform(put("/api/v1/admin/students/10001/portrait")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gender": "男",
                                  "gpa": 5.2,
                                  "gradeRank": 1,
                                  "majorRank": 1,
                                  "creditsEarned": 100,
                                  "careerOrientation": "升学",
                                  "updatedBy": "系统管理员",
                                  "dataSource": "老师维护",
                                  "publicVisible": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("学生画像 GPA 必须在 0 到 4.5 之间")));
    }

    @Test
    void platformAndAdminPagingRejectInvalidPageParams() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(get("/api/v1/platform/users/page")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("page 不能小于 0"));

        mockMvc.perform(get("/api/v1/admin/notices/page")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("size 不能小于 1"));
    }

    @Test
    void pathAndQueryIdentifiersRejectNonPositiveValues() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(get("/api/v1/platform/users/me/student-scope/check-student")
                        .header("Authorization", "Bearer " + token)
                        .param("studentId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("学生ID必须大于 0"));

        mockMvc.perform(get("/api/v1/knowledge/0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("知识条目ID必须大于 0"));
    }

    @Test
    void platformUserResetPasswordAndNotificationRejectInvalidBusinessRules() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/platform/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": " bad-user ",
                                  "role": "COUNSELOR",
                                  "enabled": true,
                                  "rawPassword": "123456",
                                  "passwordResetRequired": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名首尾不能包含空格"));

        mockMvc.perform(post("/api/v1/platform/users/1/reset-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "newPassword": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("重置密码长度不能少于 6 位"));

        mockMvc.perform(post("/api/v1/platform/notifications/send")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "重复渠道测试",
                                  "channel": "IN_APP",
                                  "targetType": "CLASS",
                                  "targetDescription": "2023级/计科一班",
                                  "status": "SENT",
                                  "recipientCount": 38,
                                  "extensionChannels": ["EMAIL", "IN_APP"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("扩展渠道不能与主发送渠道重复"));
    }

    @Test
    void platformImportTaskRejectsInvalidFileTypeAndTransition() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/platform/import-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskType": "STUDENT_PROFILE",
                                  "fileName": "students-import.pdf",
                                  "totalRows": 12
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("导入文件仅支持 xlsx、xls、csv"));

        mockMvc.perform(put("/api/v1/platform/import-tasks/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "SUCCESS",
                                  "successRows": 20,
                                  "failedRows": 1,
                                  "errorSummary": "非法成功态"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("SUCCESS 状态下")));

        mockMvc.perform(post("/api/v1/platform/import-tasks/2/execution-result")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "executionBatchNo": "batch-20260328-err-001",
                                  "callbackSource": "IMPORT_WORKER",
                                  "status": "FAILED",
                                  "successRows": 0,
                                  "failedRows": 28,
                                  "errorSummary": "导入执行失败",
                                  "errors": [
                                    {
                                      "rowNumber": 99,
                                      "fieldName": "title",
                                      "errorMessage": "超过总行数",
                                      "rawValue": "bad"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("错误行号不能超过导入任务总行数"));
    }

    @Test
    void platformListFiltersRejectUnsupportedEnumValues() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(get("/api/v1/platform/users")
                        .header("Authorization", "Bearer " + token)
                        .param("role", "TEACHER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("角色不存在: TEACHER"));

        mockMvc.perform(get("/api/v1/platform/import-tasks/page")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "DONE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("导入任务状态不支持: DONE"));

        mockMvc.perform(get("/api/v1/platform/notifications/send-records/page")
                        .header("Authorization", "Bearer " + token)
                        .param("channel", "SMS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("通知渠道不支持: SMS"));
    }

    @Test
    void worklogAndApprovalFiltersRejectUnsupportedValues() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(get("/api/v1/worklogs/admin/filter")
                        .header("Authorization", "Bearer " + token)
                        .param("recorderRole", "TEACHER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("记录人角色不支持: TEACHER"));

        mockMvc.perform(get("/api/v1/admin/approvals/page")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "DONE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("审批状态不支持: DONE"));

        mockMvc.perform(get("/api/v1/admin/approvals/page")
                        .header("Authorization", "Bearer " + token)
                        .param("certificateType", "获奖证明"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("证明类型仅支持 在读证明、党员身份证明、困难认定证明"));
    }

    @Test
    void filtersAcceptNormalizedCaseAndTrimmedValues() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(get("/api/v1/worklogs/admin/filter")
                        .header("Authorization", "Bearer " + token)
                        .param("recorderRole", " counselor "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/admin/approvals/page")
                        .header("Authorization", "Bearer " + token)
                        .param("status", " pending ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/v1/admin/notices/page")
                        .header("Authorization", "Bearer " + token)
                        .param("tag", " 流程 ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/v1/platform/users")
                        .header("Authorization", "Bearer " + token)
                        .param("role", " counselor ")
                        .param("keyword", " teacher "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role").value("COUNSELOR"))
                .andExpect(jsonPath("$.data[0].username").value("teacher01"));

        mockMvc.perform(get("/api/v1/platform/import-tasks/page")
                        .header("Authorization", "Bearer " + token)
                        .param("status", " partial_success ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].status").value("PARTIAL_SUCCESS"));

        mockMvc.perform(get("/api/v1/admin/operation-logs/page")
                        .header("Authorization", "Bearer " + token)
                        .param("operatorRole", " counselor ")
                        .param("targetKeyword", " 1001 ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].operatorRole").value("COUNSELOR"))
                .andExpect(jsonPath("$.data.content[0].target").value("证明申请#1001"));

        mockMvc.perform(get("/api/v1/admin/knowledge/page")
                        .header("Authorization", "Bearer " + token)
                        .param("category", " 内部资料 ")
                        .param("keyword", " 口径 ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].category").value("内部资料"))
                .andExpect(jsonPath("$.data.content[0].title").value("辅导员内部口径说明"));

        mockMvc.perform(get("/api/v1/admin/advisor-scopes/page")
                        .header("Authorization", "Bearer " + token)
                        .param("advisorUsername", " advisor01 ")
                        .param("className", " 计科一班 ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].advisorUsername").value("advisor01"))
                .andExpect(jsonPath("$.data.content[0].className").value("计科一班"));

        mockMvc.perform(get("/api/v1/admin/import-tasks/2/errors/page")
                        .header("Authorization", "Bearer " + token)
                        .param("fieldName", " officialUrl ")
                        .param("keyword", " bad-url ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fieldName").value("officialUrl"));

        mockMvc.perform(get("/api/v1/platform/notifications/send-records/page")
                        .header("Authorization", "Bearer " + token)
                        .param("channel", " in_app ")
                        .param("status", " sent ")
                        .param("targetKeyword", " studentId=10001 ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].targetDescription").value("studentId=10001"));

        mockMvc.perform(get("/api/v1/admin/students/page")
                        .header("Authorization", "Bearer " + token)
                        .param("grade", " 2023级 ")
                        .param("status", " active ")
                        .param("keyword", " 张三 ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("张三"))
                .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));

        mockMvc.perform(get("/api/v1/admin/students/portraits/page")
                        .header("Authorization", "Bearer " + token)
                        .param("grade", " 2023级 ")
                        .param("careerOrientation", " 升学 ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].studentId").value(10001));

        mockMvc.perform(get("/api/v1/knowledge/search")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", " 保密 "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].officialLinkOnly").value(true));

        mockMvc.perform(get("/api/v1/admin/students/stats")
                        .header("Authorization", "Bearer " + token)
                        .param("status", " graduated "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudents").isNumber());
    }

    @Test
    void studentFiltersRejectUnsupportedStatusValues() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(get("/api/v1/admin/students/page")
                        .header("Authorization", "Bearer " + token)
                        .param("status", " done ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("学生状态仅支持 ACTIVE、SUSPENDED、GRADUATED、TRANSFERRED、WITHDRAWN"));

        mockMvc.perform(get("/api/v1/admin/students/stats")
                        .header("Authorization", "Bearer " + token)
                        .param("status", " archived "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("学生状态仅支持 ACTIVE、SUSPENDED、GRADUATED、TRANSFERRED、WITHDRAWN"));
    }

    @Test
    void studentAdminRejectsPlaintextSensitiveFields() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentNo": "2023999998",
                                  "name": "明文敏感信息测试",
                                  "major": "计算机类",
                                  "grade": "2023级",
                                  "className": "计科测试班",
                                  "advisorScope": "advisor01|王老师",
                                  "degreeLevel": "本科",
                                  "email": "safe@example.edu",
                                  "graduated": false,
                                  "status": "ACTIVE",
                                  "majorChangedTo": null,
                                  "encryptedIdCardNo": "110101199901011234",
                                  "encryptedPhone": "enc:13800001234"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("encryptedIdCardNo 不能直接提交明文身份证号，请传入加密后内容"));

        mockMvc.perform(post("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentNo": "2023999997",
                                  "name": "明文手机号测试",
                                  "major": "计算机类",
                                  "grade": "2023级",
                                  "className": "计科测试班",
                                  "advisorScope": "advisor01|王老师",
                                  "degreeLevel": "本科",
                                  "email": "safe2@example.edu",
                                  "graduated": false,
                                  "status": "ACTIVE",
                                  "majorChangedTo": null,
                                  "encryptedIdCardNo": "enc:110101199901011234",
                                  "encryptedPhone": "13800001234"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("encryptedPhone 不能直接提交明文手机号，请传入加密后内容"));
    }

    @Test
    void certificateCreateRejectsUnsupportedCertificateType() throws Exception {
        String token = loginAndExtractToken("2023100001", "123456");

        mockMvc.perform(post("/api/v1/certificates/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 10001,
                                  "certificateType": "奖学金证明",
                                  "reason": "测试非法证明类型"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("证明类型仅支持 在读证明、党员身份证明、困难认定证明"));
    }

    private String loginAndExtractToken(String username, String password) throws Exception {
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginResponse).path("data").path("token").asText();
    }
}
