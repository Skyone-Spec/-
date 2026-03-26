package edu.ruc.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class PartyAcademicIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void partyProgressEndpointsRequireTokenAndReturnData() throws Exception {
        mockMvc.perform(get("/api/v1/party-progress/10001"))
                .andExpect(status().isForbidden());

        String token = loginAndExtractToken();

        mockMvc.perform(get("/api/v1/party-progress/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStage").isString())
                .andExpect(jsonPath("$.data.stageDurationDays").isNumber())
                .andExpect(jsonPath("$.data.nextDeadline").isString())
                .andExpect(jsonPath("$.data.nextActionRule").isString());

        mockMvc.perform(get("/api/v1/party-progress/10001/timeline")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stages").isArray())
                .andExpect(jsonPath("$.data.stages[2].expectedDurationDays").value(90))
                .andExpect(jsonPath("$.data.stages[2].reminderRule").value("培养期满 3 个月提交思想汇报"));

        mockMvc.perform(get("/api/v1/party-progress/10001/reminders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].stageName").value("积极分子"))
                .andExpect(jsonPath("$.data[0].triggerRule").value("培养期满 3 个月提交思想汇报"))
                .andExpect(jsonPath("$.data[0].daysRemaining").isNumber())
                .andExpect(jsonPath("$.data[0].overdue").value(false));
    }

    @Test
    void academicAnalysisRequiresTokenAndReturnsSummary() throws Exception {
        mockMvc.perform(get("/api/v1/academic/analysis/10001"))
                .andExpect(status().isForbidden());

        String token = loginAndExtractToken();

        mockMvc.perform(get("/api/v1/academic/analysis/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(10001))
                .andExpect(jsonPath("$.data.summary").isString())
                .andExpect(jsonPath("$.data.riskSummary.level").value("HIGH"))
                .andExpect(jsonPath("$.data.recommendedCourses").isArray())
                .andExpect(jsonPath("$.data.totalRequiredCredits").value(26))
                .andExpect(jsonPath("$.data.totalEarnedCredits").value(16))
                .andExpect(jsonPath("$.data.totalMissingCredits").value(10))
                .andExpect(jsonPath("$.data.completionRate").value(62))
                .andExpect(jsonPath("$.data.highlightedModules[0]").value("专业核心课"))
                .andExpect(jsonPath("$.data.missingModules[0].missingCredits").value(6))
                .andExpect(jsonPath("$.data.missingModules[0].completionRate").value(67))
                .andExpect(jsonPath("$.data.dataSourceNote").isString())
                .andExpect(jsonPath("$.data.missingModules").isArray());
    }

    private String loginAndExtractToken() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "2023100001",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginResponse).path("data").path("token").asText();
    }
}
