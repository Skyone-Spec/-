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
class BusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void noticesEndpointRequiresTokenAndReturnsStudentNotices() throws Exception {
        mockMvc.perform(get("/api/v1/notices/student/10001"))
                .andExpect(status().isForbidden());

        String token = loginAndExtractToken("2023100001", "123456");

        mockMvc.perform(get("/api/v1/notices/student/10001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("2023级计算机类就业信息汇总"))
                .andExpect(jsonPath("$.data[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.data[0].matchedRules[0]").value("年级匹配"))
                .andExpect(jsonPath("$.data[0].deliveryChannels[2]").value("WECHAT"));
    }

    @Test
    void adminApprovalActionUpdatesStatus() throws Exception {
        String token = loginAndExtractToken("admin", "123456");

        mockMvc.perform(post("/api/v1/admin/approvals/1001/action")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "approve",
                                  "comment": "材料齐全"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requestId").value(1001))
                .andExpect(jsonPath("$.data.status").value("COUNSELOR_APPROVED"))
                .andExpect(jsonPath("$.data.reason").value("材料齐全"));

        mockMvc.perform(get("/api/v1/admin/approvals/page")
                        .param("status", "COUNSELOR_APPROVED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/v1/admin/approvals/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTasks").isNumber())
                .andExpect(jsonPath("$.data.pendingTasks").isNumber());
    }

    @Test
    void approvalFlowEnforcesRoleAndStateMachine() throws Exception {
        String counselorToken = loginAndExtractToken("teacher01", "123456");
        String adminToken = loginAndExtractToken("admin", "123456");
        String studentToken = loginAndExtractToken("2023100001", "123456");

        mockMvc.perform(post("/api/v1/admin/approvals/1002/action")
                        .header("Authorization", "Bearer " + counselorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "approve",
                                  "comment": "辅导员不能越级终审"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("辅导员初审通过后的申请仅学院管理员可终审"));

        mockMvc.perform(post("/api/v1/admin/approvals/1002/action")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "approve",
                                  "comment": "学院终审通过"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(post("/api/v1/certificates/requests")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentId": 10001,
                                  "certificateType": "困难认定证明",
                                  "reason": "申请资助"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(post("/api/v1/certificates/requests/2001/action")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "withdraw",
                                  "comment": "材料有误，先撤回"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"));

        mockMvc.perform(post("/api/v1/certificates/requests/2001/action")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "withdraw",
                                  "comment": "重复撤回"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("仅待初审状态的申请支持撤回"));

        mockMvc.perform(post("/api/v1/certificates/requests/2001/action")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "resubmit",
                                  "comment": "重新提交材料"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(get("/api/v1/certificates/requests/1002/preview")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.certificateType").value("党员身份证明"))
                .andExpect(jsonPath("$.data.templateFields.partyStatus").value("党员身份待学院核验"))
                .andExpect(jsonPath("$.data.generatedContent").value(org.hamcrest.Matchers.containsString("组织发展档案核验")))
                .andExpect(jsonPath("$.data.nextStepHint").value("党员身份证明已完成审核，可下载正式文件并用于组织关系转接等场景。"));
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
