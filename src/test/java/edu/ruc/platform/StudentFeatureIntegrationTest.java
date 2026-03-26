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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
class StudentFeatureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void knowledgeEndpointsRequireTokenAndReturnData() throws Exception {
        mockMvc.perform(get("/api/v1/knowledge/search").param("keyword", "党员"))
                .andExpect(status().isForbidden());

        String token = loginAndExtractToken();

        mockMvc.perform(get("/api/v1/knowledge/search")
                        .param("keyword", "党员")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].responseStrategy").isString())
                .andExpect(jsonPath("$.data[0].officialLinkOnly").isBoolean());

        mockMvc.perform(get("/api/v1/knowledge/templates")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").isString());

        mockMvc.perform(get("/api/v1/knowledge/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.responseStrategy").value("STANDARD_ANSWER"))
                .andExpect(jsonPath("$.data.attachments").isArray())
                .andExpect(jsonPath("$.data.relatedItems").isArray());

        mockMvc.perform(get("/api/v1/knowledge/search")
                        .param("keyword", "保密")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].responseStrategy").value("OFFICIAL_LINK_ONLY"))
                .andExpect(jsonPath("$.data[0].officialLinkOnly").value(true))
                .andExpect(jsonPath("$.data[0].officialUrl").isString())
                .andExpect(jsonPath("$.data[0].answer").value("该主题涉及敏感信息或权限边界，前台仅展示公开口径，请优先查看官方链接或联系负责老师。"));

        mockMvc.perform(get("/api/v1/knowledge/4")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.officialLinkOnly").value(true))
                .andExpect(jsonPath("$.data.responseStrategy").value("OFFICIAL_LINK_ONLY"))
                .andExpect(jsonPath("$.data.safetyTip").value("敏感字段不在知识库详情中直接展开，需按权限走官方渠道查询。"));
    }

    @Test
    void certificateEndpointsCreateAndQueryStudentRequests() throws Exception {
        String token = loginAndExtractToken();

        mockMvc.perform(post("/api/v1/certificates/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 10001,
                                  "certificateType": "在读证明",
                                  "reason": "课程证明"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(10001))
                .andExpect(jsonPath("$.data.certificateType").value("在读证明"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(get("/api/v1/certificates/requests/student/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].studentId").value(10001));
    }

    @Test
    void studentCanReadOwnProfileButCannotReadAdminStudentsEndpoint() throws Exception {
        String token = loginAndExtractToken();

        mockMvc.perform(get("/api/v1/student/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentNo").value("2023100001"));

        mockMvc.perform(get("/api/v1/admin/students")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void studentDashboardAndSelfServiceEndpointsReturnAggregatedData() throws Exception {
        String token = loginAndExtractToken();

        mockMvc.perform(get("/api/v1/student/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.studentNo").value("2023100001"))
                .andExpect(jsonPath("$.data.partyProgress.currentStage").isString())
                .andExpect(jsonPath("$.data.notices").isArray())
                .andExpect(jsonPath("$.data.certificateRequests").isArray())
                .andExpect(jsonPath("$.data.recommendedKnowledge").isArray())
                .andExpect(jsonPath("$.data.focusItems").isArray())
                .andExpect(jsonPath("$.data.focusItems[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.data.focusItems[0].actionPath").isString())
                .andExpect(jsonPath("$.data.focusItems[?(@.type=='WORKLOG')]").isNotEmpty())
                .andExpect(jsonPath("$.data.focusItems[?(@.type=='PORTRAIT')]").isNotEmpty());

        mockMvc.perform(get("/api/v1/student/notices")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].matchedRules").isArray())
                .andExpect(jsonPath("$.data[0].deliveryChannels").isArray());

        mockMvc.perform(get("/api/v1/student/certificates/requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/student/party-progress")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStage").isString());

        mockMvc.perform(get("/api/v1/student/knowledge/recommended")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.title=='电子证明与常用表格入口')]").isNotEmpty())
                .andExpect(jsonPath("$.data[0].title").isString());

        mockMvc.perform(get("/api/v1/student/growth-suggestions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studentId").value(10001))
                .andExpect(jsonPath("$.data.suggestions").isArray())
                .andExpect(jsonPath("$.data.suggestions[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.data.suggestions[0].actionPath").isString())
                .andExpect(jsonPath("$.data.suggestions[?(@.category=='ACADEMIC')]").isNotEmpty())
                .andExpect(jsonPath("$.data.suggestions[?(@.category=='WORKLOG')]").isNotEmpty())
                .andExpect(jsonPath("$.data.suggestions[?(@.category=='PORTRAIT')]").isNotEmpty());
    }

    @Test
    void studentCanPreviewWithdrawAndResubmitOwnCertificateRequest() throws Exception {
        String token = loginAndExtractToken();

        String createResponse = mockMvc.perform(post("/api/v1/certificates/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 10001,
                                  "certificateType": "在读证明",
                                  "reason": "课程证明"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        long requestId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/certificates/requests/{requestId}/preview", requestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestId").value(requestId))
                .andExpect(jsonPath("$.data.canWithdraw").value(true))
                .andExpect(jsonPath("$.data.currentApproverRole").value("COUNSELOR"))
                .andExpect(jsonPath("$.data.approvalLevel").value(1))
                .andExpect(jsonPath("$.data.outputFormat").value("PDF"))
                .andExpect(jsonPath("$.data.templateFields.studentId").value("10001"))
                .andExpect(jsonPath("$.data.templateFields.certificateType").value("在读证明"))
                .andExpect(jsonPath("$.data.templateFields.studentStatus").value("在籍"))
                .andExpect(jsonPath("$.data.generatedContent").value(org.hamcrest.Matchers.containsString("系本院在籍学生")))
                .andExpect(jsonPath("$.data.nextStepHint").isString());

        mockMvc.perform(post("/api/v1/certificates/requests/{requestId}/action", requestId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "withdraw",
                                  "comment": "学生主动撤回"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"));

        mockMvc.perform(get("/api/v1/certificates/requests/{requestId}/history", requestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].operatorRole").value("STUDENT"));

        mockMvc.perform(post("/api/v1/certificates/requests/{requestId}/action", requestId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "resubmit",
                                  "comment": "重新提交材料"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void studentCannotReadOtherStudentsNoticesOrRequests() throws Exception {
        String token = loginAndExtractToken();

        mockMvc.perform(get("/api/v1/notices/student/10002")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/v1/certificates/requests/student/10002")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private String loginAndExtractToken() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
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
