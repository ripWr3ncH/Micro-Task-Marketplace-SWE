package com.logarithm.microtask.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logarithm.microtask.entity.enums.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketplaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRejectUnauthenticatedTaskListRequest() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRegisterAndLoginBuyer() throws Exception {
        String registerBody = """
                {
                  "fullName":"Buyer One",
                  "email":"buyer1@test.com",
                  "password":"password123",
                  "roles":["BUYER"]
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = """
                {
                  "email":"buyer1@test.com",
                  "password":"password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateTaskApplyAndAccept() throws Exception {
        String buyerToken = registerAndGetToken("buyer2@test.com", RoleName.BUYER);
        String sellerToken = registerAndGetToken("seller2@test.com", RoleName.SELLER);

        String taskBody = """
                {
                  "title":"Design Logo",
                  "description":"Need a logo for startup",
                  "budget":100.00
                }
                """;

        String taskResponse = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long taskId = objectMapper.readTree(taskResponse).get("id").asLong();

        String applyBody = """
                {
                  "taskId":%d,
                  "proposedAmount":90.00,
                  "coverLetter":"Experienced freelancer"
                }
                """.formatted(taskId);

        String applicationResponse = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long applicationId = objectMapper.readTree(applicationResponse).get("id").asLong();

        mockMvc.perform(post("/api/v1/applications/{applicationId}/accept", applicationId)
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk());
    }

    private String registerAndGetToken(String email, RoleName roleName) throws Exception {
        String registerBody = """
                {
                  "fullName":"%s",
                  "email":"%s",
                  "password":"password123",
                  "roles":["%s"]
                }
                """.formatted(email.split("@")[0], email, roleName.name());

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(registerResponse);
        return node.get("token").asText();
    }
}
