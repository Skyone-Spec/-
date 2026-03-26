package edu.ruc.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class StudentWorkLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void leagueSecretaryCanCreateWorkLogAndStudentCanReadOwnSummary() throws Exception {
        String leaderToken = login("2023100002", "123456");
        String studentToken = login("2023100001", "123456");

        mockMvc.perform(post("/api/v1/worklogs")
                        .header("Authorization", "Bearer " + leaderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 10001,
                                  "studentName": "张三",
                                  "category": "党团事务",
                                  "title": "会议材料整理",
                                  "description": "协助整理支部会议材料",
                                  "workloadScore": 4,
                                  "workDate": "2026-03-23"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("会议材料整理"))
                .andExpect(jsonPath("$.data.recorderRole").value("LEAGUE_SECRETARY"));

        mockMvc.perform(get("/api/v1/worklogs/student/10001/summary")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(10001))
                .andExpect(jsonPath("$.data.totalEntries").isNumber())
                .andExpect(jsonPath("$.data.totalWorkloadScore").isNumber());
    }

    @Test
    void advisorCanReadWorklogOverview() throws Exception {
        String token = login("advisor01", "123456");

        mockMvc.perform(get("/api/v1/worklogs/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEntries").isNumber())
                .andExpect(jsonPath("$.data.categoryStats").isArray());
    }

    @Test
    void advisorCanFilterWorklogs() throws Exception {
        String token = login("advisor01", "123456");

        mockMvc.perform(get("/api/v1/worklogs/admin/filter")
                        .param("category", "党团事务")
                        .param("grade", "2023级")
                        .param("className", "计科一班")
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-03-12")
                        .param("sortBy", "workDate")
                        .param("sortDir", "desc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void advisorCanReadWorklogAdminStats() throws Exception {
        String token = login("advisor01", "123456");

        mockMvc.perform(get("/api/v1/worklogs/admin/stats")
                        .param("category", "党团事务")
                        .param("grade", "2023级")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEntries").isNumber())
                .andExpect(jsonPath("$.data.categoryStats").isArray())
                .andExpect(jsonPath("$.data.topStudents").isArray())
                .andExpect(jsonPath("$.data.monthStats").isArray())
                .andExpect(jsonPath("$.data.dailyTrend").isArray())
                .andExpect(jsonPath("$.data.recorderRoleStats").isArray())
                .andExpect(jsonPath("$.data.scoreBandStats").isArray())
                .andExpect(jsonPath("$.data.gradeStats").isArray())
                .andExpect(jsonPath("$.data.classStats").isArray());
    }

    @Test
    void advisorCanReadWorklogAdminPageAndExportMetadata() throws Exception {
        String token = login("advisor01", "123456");

        mockMvc.perform(get("/api/v1/worklogs/admin/page")
                        .param("className", "计科一班")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sortBy", "workDate")
                        .param("sortDir", "desc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));

        mockMvc.perform(get("/api/v1/worklogs/admin/export-metadata")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultFileName").value("student-worklogs-export"))
                .andExpect(jsonPath("$.data.fields").isArray());

        mockMvc.perform(get("/api/v1/worklogs/admin/page")
                        .param("sortBy", "unsupported")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void leagueSecretaryCanUpdateDeleteAndTraceOwnWorklog() throws Exception {
        String leagueToken = login("2023100002", "123456");
        String advisorToken = login("advisor01", "123456");

        String createResponse = mockMvc.perform(post("/api/v1/worklogs")
                        .header("Authorization", "Bearer " + leagueToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 10001,
                                  "studentName": "张三",
                                  "category": "学生工作",
                                  "title": "初始工作标题",
                                  "description": "初始描述",
                                  "workloadScore": 3,
                                  "workDate": "2026-03-23"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long worklogId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(put("/api/v1/worklogs/{id}", worklogId)
                        .header("Authorization", "Bearer " + leagueToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "党团事务",
                                  "title": "更新后的工作标题",
                                  "description": "更新后的描述",
                                  "workloadScore": 5,
                                  "workDate": "2026-03-24"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("更新后的工作标题"))
                .andExpect(jsonPath("$.data.workloadScore").value(5));

        mockMvc.perform(get("/api/v1/worklogs/{id}/actions", worklogId)
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].action").value("CREATE"))
                .andExpect(jsonPath("$.data[1].action").value("UPDATE"));

        mockMvc.perform(delete("/api/v1/worklogs/{id}", worklogId)
                        .header("Authorization", "Bearer " + leagueToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/worklogs/student/10001")
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id==%s)]".formatted(worklogId)).isEmpty());
    }

    @Test
    void advisorCannotReadOtherClassStudentWorklogs() throws Exception {
        String token = login("advisor01", "123456");

        mockMvc.perform(get("/api/v1/worklogs/student/10002")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void studentCannotCreateWorklogForAnotherStudent() throws Exception {
        String token = login("2023100001", "123456");

        mockMvc.perform(post("/api/v1/worklogs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 10002,
                                  "studentName": "李四",
                                  "category": "学生工作",
                                  "title": "越权登记",
                                  "description": "不应允许",
                                  "workloadScore": 2,
                                  "workDate": "2026-03-23"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private String login(String username, String password) throws Exception {
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
