package edu.ruc.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ruc.platform.auth.domain.UserAccount;
import edu.ruc.platform.auth.repository.LoginAuditLogRepository;
import edu.ruc.platform.auth.repository.UserAccountRepository;
import edu.ruc.platform.auth.repository.UserSessionRecordRepository;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import edu.ruc.platform.worklog.repository.StudentWorkLogActionLogRepository;
import edu.ruc.platform.worklog.repository.StudentWorkLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:devprofilebiz;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC",
        "spring.flyway.enabled=false"
})
class DevProfileBusinessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private UserSessionRecordRepository userSessionRecordRepository;

    @Autowired
    private LoginAuditLogRepository loginAuditLogRepository;

    @Autowired
    private StudentWorkLogRepository studentWorkLogRepository;

    @Autowired
    private StudentWorkLogActionLogRepository studentWorkLogActionLogRepository;

    private Long studentId;

    @BeforeEach
    void setUp() {
        studentWorkLogActionLogRepository.deleteAll();
        studentWorkLogRepository.deleteAll();
        userSessionRecordRepository.deleteAll();
        loginAuditLogRepository.deleteAll();
        studentProfileRepository.deleteAll();
        userAccountRepository.deleteAll();

        UserAccount admin = new UserAccount();
        admin.setUsername("admin-dev");
        admin.setPasswordHash(passwordEncoder.encode("123456"));
        admin.setRole(RoleType.SUPER_ADMIN);
        admin.setEnabled(Boolean.TRUE);
        admin.setPasswordResetRequired(Boolean.FALSE);
        admin.setFailedLoginAttempts(0);
        userAccountRepository.save(admin);

        StudentProfile student = new StudentProfile();
        student.setStudentNo("2023990001");
        student.setName("测试学生");
        student.setMajor("计算机类");
        student.setGrade("2023级");
        student.setClassName("计科一班");
        student.setAdvisorScope("advisor01|王老师");
        student.setDegreeLevel("本科");
        student.setEmail("dev-student@example.edu");
        student.setGraduated(Boolean.FALSE);
        student.setStatus("ACTIVE");
        student = studentProfileRepository.save(student);
        studentId = student.getId();

        UserAccount studentUser = new UserAccount();
        studentUser.setUsername(student.getStudentNo());
        studentUser.setPasswordHash(passwordEncoder.encode("123456"));
        studentUser.setRole(RoleType.STUDENT);
        studentUser.setEnabled(Boolean.TRUE);
        studentUser.setPasswordResetRequired(Boolean.FALSE);
        studentUser.setFailedLoginAttempts(0);
        userAccountRepository.save(studentUser);
    }

    @Test
    void devProfileLoginPersistsSessionAndAudit() throws Exception {
        String token = loginAndExtractToken("admin-dev", "123456");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin-dev"))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"));

        assertThat(userSessionRecordRepository.findAll()).hasSize(1);
        assertThat(userSessionRecordRepository.findAll().get(0).getUsername()).isEqualTo("admin-dev");
        assertThat(loginAuditLogRepository.findAll()).hasSize(1);
        assertThat(loginAuditLogRepository.findAll().get(0).getResult()).isEqualTo("SUCCESS");
    }

    @Test
    void devProfileCreatedPlatformUserCanLogin() throws Exception {
        String adminToken = loginAndExtractToken("admin-dev", "123456");

        mockMvc.perform(post("/api/v1/platform/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "assistant-dev",
                                  "role": "ASSISTANT",
                                  "enabled": true,
                                  "rawPassword": "654321",
                                  "passwordResetRequired": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("assistant-dev"))
                .andExpect(jsonPath("$.data.role").value("ASSISTANT"));

        String assistantToken = loginAndExtractToken("assistant-dev", "654321");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + assistantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("assistant-dev"))
                .andExpect(jsonPath("$.data.role").value("ASSISTANT"));
    }

    @Test
    void devProfileWorklogFlowUsesRepositoryBackedServices() throws Exception {
        String adminToken = loginAndExtractToken("admin-dev", "123456");

        String createResponse = mockMvc.perform(post("/api/v1/worklogs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": %d,
                                  "studentName": "测试学生",
                                  "category": "学生工作",
                                  "title": "数据库模式工作记录",
                                  "description": "验证 dev profile 下的工作记录闭环",
                                  "workloadScore": 4,
                                  "workDate": "2026-04-07"
                                }
                                """.formatted(studentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(studentId))
                .andExpect(jsonPath("$.data.recorderRole").value("SUPER_ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createResponse);
        long worklogId = createJson.path("data").path("id").asLong();

        String studentToken = loginAndExtractToken("2023990001", "123456");

        mockMvc.perform(get("/api/v1/worklogs/student/{studentId}", studentId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(worklogId))
                .andExpect(jsonPath("$.data[0].title").value("数据库模式工作记录"));

        mockMvc.perform(get("/api/v1/worklogs/student/{studentId}/summary", studentId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEntries").value(1))
                .andExpect(jsonPath("$.data.totalWorkloadScore").value(4));

        assertThat(studentWorkLogRepository.findAll()).hasSize(1);
        assertThat(studentWorkLogActionLogRepository.findAll()).hasSize(1);
        assertThat(studentWorkLogActionLogRepository.findAll().get(0).getAction()).isEqualTo("CREATE");
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
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).path("data").path("token").asText();
    }
}
