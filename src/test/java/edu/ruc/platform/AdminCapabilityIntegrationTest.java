package edu.ruc.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
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
class AdminCapabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanManageKnowledgeAttachmentsAndImportErrors() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        String createKnowledgeResponse = mockMvc.perform(post("/api/v1/admin/knowledge")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "附件测试知识条目",
                                  "category": "党团事务",
                                  "content": "用于测试附件上传",
                                  "officialUrl": "https://example.edu/knowledge",
                                  "sourceFileName": "knowledge-base.pdf",
                                  "audienceScope": "全体学生",
                                  "updatedBy": "系统管理员",
                                  "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long knowledgeId = objectMapper.readTree(createKnowledgeResponse).path("data").path("id").asLong();

        MockMultipartFile validAttachment = new MockMultipartFile(
                "file",
                "policy.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "policy-content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/admin/knowledge/{id}/attachments", knowledgeId)
                        .file(validAttachment)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.knowledgeId").value(knowledgeId))
                .andExpect(jsonPath("$.data.fileName").isString());

        mockMvc.perform(get("/api/v1/admin/knowledge/{id}/attachments", knowledgeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].knowledgeId").value(knowledgeId));

        mockMvc.perform(post("/api/v1/admin/import-tasks/2/errors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rowNumber": 15,
                                  "fieldName": "category",
                                  "errorMessage": "分类不存在",
                                  "rawValue": "未知分类"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(2))
                .andExpect(jsonPath("$.data.rowNumber").value(15));

        mockMvc.perform(get("/api/v1/admin/import-tasks/2/errors")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].taskId").value(2));

        mockMvc.perform(get("/api/v1/admin/import-tasks/2/errors/page")
                        .param("fieldName", "category")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    void adminKnowledgeAndImportGovernanceRulesAreEnforced() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/admin/knowledge")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "缺少来源的已发布知识",
                                  "category": "党团事务",
                                  "content": "无来源不应允许发布",
                                  "officialUrl": "",
                                  "sourceFileName": "",
                                  "audienceScope": "全体学生",
                                  "updatedBy": "系统管理员",
                                  "published": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("已发布知识条目必须提供官方链接或来源文件名"));

        mockMvc.perform(post("/api/v1/admin/knowledge")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "错误链接知识",
                                  "category": "党团事务",
                                  "content": "错误链接格式",
                                  "officialUrl": "ftp://example.edu/file",
                                  "sourceFileName": "source.pdf",
                                  "audienceScope": "全体学生",
                                  "updatedBy": "系统管理员",
                                  "published": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("官方链接格式不正确"));

        String createKnowledgeResponse = mockMvc.perform(post("/api/v1/admin/knowledge")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "治理规则测试知识",
                                  "category": "党团事务",
                                  "content": "用于测试非法附件类型",
                                  "officialUrl": "https://example.edu/knowledge",
                                  "sourceFileName": "governance.pdf",
                                  "audienceScope": "全体学生",
                                  "updatedBy": "系统管理员",
                                  "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long knowledgeId = objectMapper.readTree(createKnowledgeResponse).path("data").path("id").asLong();

        MockMultipartFile invalidAttachment = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                "binary".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/admin/knowledge/{id}/attachments", knowledgeId)
                        .file(invalidAttachment)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("知识附件仅支持 pdf、doc、docx、xls、xlsx、txt"));

        mockMvc.perform(post("/api/v1/admin/import-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskType": "UNKNOWN_TYPE",
                                  "fileName": "data.xlsx",
                                  "owner": "系统管理员",
                                  "totalRows": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("导入任务类型仅支持 STUDENT_PROFILE、KNOWLEDGE_BASE、NOTICE、ADVISOR_SCOPE"));

        mockMvc.perform(post("/api/v1/admin/import-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskType": "STUDENT_PROFILE",
                                  "fileName": "data.pdf",
                                  "owner": "系统管理员",
                                  "totalRows": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("导入文件仅支持 xlsx、xls、csv"));

        mockMvc.perform(put("/api/v1/admin/import-tasks/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RUNNING",
                                  "successRows": 10,
                                  "failedRows": 0,
                                  "errorSummary": "不应允许更新已完成任务"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("已完成的导入任务不允许再次更新状态"));

        mockMvc.perform(put("/api/v1/admin/import-tasks/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "SUCCESS",
                                  "successRows": 26,
                                  "failedRows": 2,
                                  "errorSummary": "成功状态行数不一致"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("SUCCESS 状态下成功行数必须等于总行数"));

        mockMvc.perform(post("/api/v1/admin/import-tasks/2/errors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rowNumber": 99,
                                  "fieldName": "title",
                                  "errorMessage": "超过总行数",
                                  "rawValue": "bad"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("错误行号不能超过导入任务总行数"));
    }

    @Test
    void adminCanReadOperationLogsAndImportTasks() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(get("/api/v1/admin/notices/page")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));

        mockMvc.perform(get("/api/v1/admin/notices/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalNotices").isNumber())
                .andExpect(jsonPath("$.data.taggedNotices").isNumber());

        mockMvc.perform(get("/api/v1/admin/operation-logs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].module").isString())
                .andExpect(jsonPath("$.data[0].operatorRole").isString());

        mockMvc.perform(get("/api/v1/admin/operation-logs/page")
                        .param("module", "KNOWLEDGE")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(2));

        mockMvc.perform(get("/api/v1/admin/operation-logs/stats")
                        .param("module", "KNOWLEDGE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalLogs").isNumber())
                .andExpect(jsonPath("$.data.successLogs").isNumber())
                .andExpect(jsonPath("$.data.moduleStats").isArray())
                .andExpect(jsonPath("$.data.operatorRoleStats").isArray());

        mockMvc.perform(get("/api/v1/admin/import-tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].taskType").isString())
                .andExpect(jsonPath("$.data[0].status").isString());

        mockMvc.perform(get("/api/v1/admin/import-tasks/page")
                        .param("taskType", "KNOWLEDGE_BASE")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));

        mockMvc.perform(get("/api/v1/admin/import-tasks/stats")
                        .param("taskType", "KNOWLEDGE_BASE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTasks").isNumber())
                .andExpect(jsonPath("$.data.totalRows").isNumber());

        mockMvc.perform(get("/api/v1/admin/knowledge/stats")
                        .param("published", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").isNumber())
                .andExpect(jsonPath("$.data.publishedItems").isNumber())
                .andExpect(jsonPath("$.data.categoryStats").isArray());

        mockMvc.perform(get("/api/v1/admin/advisor-scopes")
                        .param("advisorUsername", "advisor01")
                        .param("grade", "2023级")
                        .param("className", "计科一班")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].advisorUsername").value("advisor01"));

        mockMvc.perform(get("/api/v1/admin/advisor-scopes/page")
                        .param("advisorUsername", "advisor01")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));

        mockMvc.perform(get("/api/v1/admin/advisor-scopes/stats")
                        .param("grade", "2023级")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBindings").isNumber())
                .andExpect(jsonPath("$.data.advisorStats").isArray());
    }

    @Test
    void classAdvisorCanReadNoticePageAndStatsButCannotReadOperationLogs() throws Exception {
        String adminToken = loginAndExtractToken("admin", "123456");
        String token = loginAndExtractToken("advisor01", "123456");

        mockMvc.perform(post("/api/v1/admin/notices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "2024级专属通知",
                                  "summary": "不应出现在 2023 级班主任视图",
                                  "tags": ["年级通知"],
                                  "targetDescription": "2024级"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/notices/page")
                        .param("targetKeyword", "2023级")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(2));

        mockMvc.perform(get("/api/v1/admin/notices/stats")
                        .param("targetKeyword", "2023级")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalNotices").isNumber());

        mockMvc.perform(get("/api/v1/admin/notices/page")
                        .param("targetKeyword", "2024级")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        mockMvc.perform(get("/api/v1/admin/notices/stats")
                        .param("targetKeyword", "2024级")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalNotices").value(0));

        mockMvc.perform(get("/api/v1/admin/operation-logs/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void leagueSecretaryCanReadKnowledgeButCannotCreateKnowledge() throws Exception {
        String token = loginAndExtractToken("2023100002", "123456");

        mockMvc.perform(get("/api/v1/admin/knowledge")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.title=='辅导员内部口径说明')]").isEmpty());

        mockMvc.perform(get("/api/v1/admin/knowledge/page")
                        .param("published", "true")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(2));

        mockMvc.perform(get("/api/v1/admin/knowledge/stats")
                        .param("published", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").isNumber());

        mockMvc.perform(post("/api/v1/admin/knowledge")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "测试",
                                  "category": "测试",
                                  "content": "测试",
                                  "officialUrl": "https://example.edu",
                                  "sourceFileName": "test.pdf",
                                  "audienceScope": "全体学生",
                                  "updatedBy": "李四",
                                  "published": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/v1/admin/knowledge/101/attachments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/v1/admin/import-tasks/page")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/v1/admin/operation-logs/page")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void classAdvisorCannotExecuteApprovalAction() throws Exception {
        String token = loginAndExtractToken("advisor01", "123456");

        mockMvc.perform(get("/api/v1/admin/approvals/page")
                        .param("studentId", "10002")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        mockMvc.perform(get("/api/v1/admin/approvals/stats")
                        .param("studentId", "10002")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTasks").value(0));

        mockMvc.perform(get("/api/v1/admin/approvals/1002/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/v1/admin/approvals/1002/action")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "approve",
                                  "comment": "班主任尝试审批"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void leagueSecretaryCanOnlyReadPublicPortrait() throws Exception {
        String token = loginAndExtractToken("2023100002", "123456");

        mockMvc.perform(get("/api/v1/admin/students/10001/portrait")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.publicVisible").value(true));

        mockMvc.perform(put("/api/v1/admin/students/10002/portrait")
                        .header("Authorization", "Bearer " + loginAndExtractToken("admin", "123456"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gender": "女",
                                  "ethnicity": "汉族",
                                  "honors": "院级表彰",
                                  "scholarships": "校级奖学金",
                                  "competitions": "服务设计",
                                  "socialPractice": "志愿走访",
                                  "volunteerService": "8小时",
                                  "researchExperience": null,
                                  "disciplineRecords": null,
                                  "dailyPerformance": "表现稳定",
                                  "gpa": 3.45,
                                  "gradeRank": 25,
                                  "majorRank": 12,
                                  "creditsEarned": 86,
                                  "careerOrientation": "就业",
                                  "remarks": "内部跟踪",
                                  "updatedBy": "胡浩老师",
                                  "dataSource": "老师维护",
                                  "publicVisible": false
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/students/10002/portrait")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void classAdvisorGradeScopeIsRestricted() throws Exception {
        String token = loginAndExtractToken("advisor01", "123456");

        mockMvc.perform(get("/api/v1/admin/students")
                        .param("grade", "2023级")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void classAdvisorGetsPartiallyMaskedStudentFields() throws Exception {
        String token = loginAndExtractToken("advisor01", "123456");

        mockMvc.perform(get("/api/v1/admin/students/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maskedIdCardNo").isString())
                .andExpect(jsonPath("$.data.maskedPhone").isString())
                .andExpect(jsonPath("$.data.maskedNativePlace").doesNotExist())
                .andExpect(jsonPath("$.data.maskedSupervisor").doesNotExist());

        mockMvc.perform(get("/api/v1/admin/students/portraits/page")
                        .param("grade", "2023级")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/v1/admin/students/portraits/stats")
                        .param("grade", "2023级")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPortraits").isNumber())
                .andExpect(jsonPath("$.data.careerStats").isArray());
    }

    @Test
    void classAdvisorWithoutGradeParamOnlyGetsOwnGradeStudents() throws Exception {
        String token = loginAndExtractToken("advisor01", "123456");

        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].grade").value("2023级"));
    }

    @Test
    void adminCanCreateKnowledgeAndImportTasks() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/admin/knowledge")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "奖学金政策补充说明",
                                  "category": "奖助学金",
                                  "content": "用于统一答复标准问题。",
                                  "officialUrl": "https://example.edu/new-policy",
                                  "sourceFileName": "scholarship-update.pdf",
                                  "audienceScope": "全体学生",
                                  "updatedBy": "胡浩老师",
                                  "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("奖学金政策补充说明"))
                .andExpect(jsonPath("$.data.updatedBy").value("系统管理员"));

        mockMvc.perform(post("/api/v1/admin/import-tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskType": "KNOWLEDGE_BASE",
                                  "fileName": "knowledge-batch.xlsx",
                                  "owner": "系统管理员",
                                  "totalRows": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskType").value("KNOWLEDGE_BASE"))
                .andExpect(jsonPath("$.data.status").value("CREATED"));

        mockMvc.perform(post("/api/v1/admin/advisor-scopes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "advisorUsername": "advisor01",
                                  "advisorName": "王老师",
                                  "grade": "2023级",
                                  "className": "计科一班",
                                  "studentId": 10001
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.advisorUsername").value("advisor01"))
                .andExpect(jsonPath("$.data.studentId").value(10001));

        mockMvc.perform(put("/api/v1/admin/advisor-scopes/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "advisorUsername": "advisor01",
                                  "advisorName": "王老师",
                                  "grade": "2023级",
                                  "className": "计科实验班",
                                  "studentId": 10001
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.className").value("计科实验班"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/admin/advisor-scopes/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/admin/operation-logs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].module").value("ADVISOR_SCOPE"))
                .andExpect(jsonPath("$.data[0].action").value("DELETE"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void secondApprovalPromotesRequestToApproved() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/admin/approvals/1002/action")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "approve",
                                  "comment": "二级审批通过"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.reason").value("二级审批通过"));

        mockMvc.perform(get("/api/v1/admin/approvals/1002/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fromStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[0].toStatus").value("COUNSELOR_APPROVED"))
                .andExpect(jsonPath("$.data[1].toStatus").value("APPROVED"));
    }

    @Test
    void knowledgeUpdatedByIsBoundToCurrentOperator() throws Exception {
        String counselorToken = loginAndExtractToken("teacher01", "123456");

        mockMvc.perform(post("/api/v1/admin/knowledge")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "责任人绑定测试",
                                  "category": "党团事务",
                                  "content": "测试知识条目维护人是否跟随当前登录账号。",
                                  "officialUrl": "https://example.edu/responsibility",
                                  "sourceFileName": "responsibility.pdf",
                                  "audienceScope": "全体学生",
                                  "updatedBy": "伪造维护人",
                                  "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedBy").value("胡浩老师"));
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
