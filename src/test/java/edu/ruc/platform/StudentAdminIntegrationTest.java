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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class StudentAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminCanManageStudentProfiles() throws Exception {
        String token = loginAndExtractToken();

        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].studentNo").isString());

        mockMvc.perform(get("/api/v1/admin/students")
                        .param("className", "计科一班")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/admin/students/page")
                        .param("grade", "2023级")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.size").value(1));

        mockMvc.perform(get("/api/v1/admin/students/stats")
                        .param("grade", "2023级")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudents").isNumber())
                .andExpect(jsonPath("$.data.activeStudents").isNumber());

        mockMvc.perform(post("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentNo": "2023100999",
                                  "name": "王五",
                                  "major": "计算机类",
                                  "grade": "2023级",
                                  "className": "计科三班",
                                  "advisorScope": "advisor01|王老师",
                                  "degreeLevel": "本科",
                                  "email": "wangwu@example.edu",
                                  "graduated": false,
                                  "status": "ACTIVE",
                                  "majorChangedTo": null,
                                  "encryptedIdCardNo": "enc:110101199901011234",
                                  "encryptedPhone": "enc:13800001234",
                                  "encryptedNativePlace": "enc:山东青岛",
                                  "encryptedHouseholdAddress": "enc:山东青岛",
                                  "encryptedSupervisor": "enc:张老师"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("王五"))
                .andExpect(jsonPath("$.data.advisorScope").value("advisor01|王老师"))
                .andExpect(jsonPath("$.data.maskedPhone").isString());

        mockMvc.perform(put("/api/v1/admin/students/10001")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentNo": "2023100001",
                                  "name": "张三",
                                  "major": "计算机类",
                                  "grade": "2023级",
                                  "className": "计科一班",
                                  "advisorScope": "advisor01|王老师",
                                  "degreeLevel": "本科",
                                  "email": "zhangsan@example.edu",
                                  "graduated": false,
                                  "status": "SUSPENDED",
                                  "majorChangedTo": "数据科学",
                                  "encryptedIdCardNo": "enc:110101199901011212",
                                  "encryptedPhone": "enc:13800005678",
                                  "encryptedNativePlace": "enc:北京海淀",
                                  "encryptedHouseholdAddress": "enc:北京海淀",
                                  "encryptedSupervisor": "enc:李老师"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"))
                .andExpect(jsonPath("$.data.majorChangedTo").value("数据科学"));

        mockMvc.perform(get("/api/v1/admin/students/10001/status-history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].studentId").value(10001))
                .andExpect(jsonPath("$.data[0].toStatus").value("SUSPENDED"));

        mockMvc.perform(post("/api/v1/admin/students/10001/status-history")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "GRADUATED",
                                  "changedToMajor": "数据科学",
                                  "reason": "学生完成培养方案，状态归档"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(10001))
                .andExpect(jsonPath("$.data.fromStatus").value("SUSPENDED"))
                .andExpect(jsonPath("$.data.toStatus").value("GRADUATED"))
                .andExpect(jsonPath("$.data.reason").value("学生完成培养方案，状态归档"));

        mockMvc.perform(get("/api/v1/admin/students/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("GRADUATED"))
                .andExpect(jsonPath("$.data.majorChangedTo").value("数据科学"));

        mockMvc.perform(put("/api/v1/admin/students/10001/portrait")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "gender": "男",
                                  "ethnicity": "汉族",
                                  "honors": "国奖,校优",
                                  "scholarships": "国家奖学金",
                                  "competitions": "数学建模,程序设计",
                                  "socialPractice": "社区服务",
                                  "volunteerService": "30小时",
                                  "researchExperience": "导师课题参与",
                                  "disciplineRecords": null,
                                  "dailyPerformance": "表现优秀",
                                  "gpa": 3.91,
                                  "gradeRank": 12,
                                  "majorRank": 5,
                                  "creditsEarned": 102,
                                  "careerOrientation": "升学",
                                  "remarks": "建议重点培养",
                                  "updatedBy": "胡浩老师",
                                  "dataSource": "老师维护",
                                  "publicVisible": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gender").value("男"))
                .andExpect(jsonPath("$.data.honors").value("国奖,校优"))
                .andExpect(jsonPath("$.data.scholarships").value("国家奖学金"))
                .andExpect(jsonPath("$.data.gpa").value(3.91))
                .andExpect(jsonPath("$.data.majorRank").value(5))
                .andExpect(jsonPath("$.data.creditsEarned").value(102))
                .andExpect(jsonPath("$.data.updatedBy").value("胡浩老师"))
                .andExpect(jsonPath("$.data.publicVisible").value(true));

        mockMvc.perform(get("/api/v1/admin/students/portraits/page")
                        .param("publicVisible", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/v1/admin/students/portraits/stats")
                        .param("publicVisible", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPortraits").isNumber())
                .andExpect(jsonPath("$.data.averageGpa").isNumber())
                .andExpect(jsonPath("$.data.gpaBandStats").isArray())
                .andExpect(jsonPath("$.data.gradeRankBandStats").isArray());
    }

    @Test
    void classAdvisorOnlyReadsScopedStudentAdminData() throws Exception {
        String advisorToken = loginAndExtractToken("advisor01", "123456");

        mockMvc.perform(get("/api/v1/admin/students/10001")
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentNo").value("2023100001"));

        mockMvc.perform(get("/api/v1/admin/students/10002")
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("学生不存在或无权访问"));

        mockMvc.perform(get("/api/v1/admin/students/10001/status-history")
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/admin/students/10002/status-history")
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/v1/admin/students/10001/portrait")
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(10001));

        mockMvc.perform(get("/api/v1/admin/students/10002/portrait")
                        .header("Authorization", "Bearer " + advisorToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void leagueSecretaryCanReadGradeScopedStudentAdminPagesAndStats() throws Exception {
        String token = loginAndExtractToken("2023100002", "123456");

        mockMvc.perform(get("/api/v1/admin/students/page")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].grade").value("2023级"))
                .andExpect(jsonPath("$.data.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].maskedIdCardNo").doesNotExist());

        mockMvc.perform(get("/api/v1/admin/students/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudents").isNumber())
                .andExpect(jsonPath("$.data.totalStudents").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.activeStudents").isNumber());

        mockMvc.perform(get("/api/v1/admin/students/portraits/page")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].studentId").value(10001));

        mockMvc.perform(get("/api/v1/admin/students/portraits/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPortraits").value(1));

        mockMvc.perform(get("/api/v1/admin/students/page")
                        .param("grade", "2022级")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权访问该学生范围数据"));
    }

    private String loginAndExtractToken() throws Exception {
        return loginAndExtractToken("admin", "123456");
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
