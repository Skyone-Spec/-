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
                .andExpect(jsonPath("$.success").value(false));
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
                                  "gradeRank": 0,
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
                .andExpect(jsonPath("$.message").value("学生画像 GPA 必须在 0 到 4.5 之间"));
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
