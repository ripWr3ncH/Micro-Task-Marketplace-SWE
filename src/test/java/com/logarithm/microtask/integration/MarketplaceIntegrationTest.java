package com.logarithm.microtask.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logarithm.microtask.entity.Role;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.RoleName;
import com.logarithm.microtask.repository.RoleRepository;
import com.logarithm.microtask.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketplaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

    @Test
    void shouldRejectUnauthenticatedTaskListRequest() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthenticatedLogoutRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogoutAuthenticatedUser() throws Exception {
        String buyerToken = registerAndGetToken("buyer-logout", RoleName.BUYER);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully."));
    }

    @Test
    void shouldInvalidateTokenAfterLogout() throws Exception {
        String buyerToken = registerAndGetToken("buyer-logout-invalidate", RoleName.BUYER);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectSellerCreatingTask() throws Exception {
        String sellerToken = registerAndGetToken("seller-create-block", RoleName.SELLER);

        String taskBody = """
                {
                  "title":"Blocked Task",
                  "description":"Seller should not create",
                  "budget":20.00
                }
                """;

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectBuyerApplyingToTask() throws Exception {
        String buyerToken = registerAndGetToken("buyer-apply-block", RoleName.BUYER);

        String taskBody = """
                {
                  "title":"API Review",
                  "description":"Need endpoint review",
                  "budget":60.00
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
                  "proposedAmount":55.00,
                  "coverLetter":"Buyer should not apply"
                }
                """.formatted(taskId);

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRegisterAndLoginBuyer() throws Exception {
        String buyerEmail = uniqueEmail("buyer-login");

        String registerBody = """
                {
                  "fullName":"Buyer One",
                  "email":"%s",
                  "password":"password123",
                  "roles":["BUYER"]
                }
                """.formatted(buyerEmail);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = """
                {
                  "email":"%s",
                  "password":"password123"
                }
                """.formatted(buyerEmail);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateTaskApplyAndAccept() throws Exception {
        String buyerToken = registerAndGetToken("buyer-flow", RoleName.BUYER);
        String sellerToken = registerAndGetToken("seller-flow", RoleName.SELLER);

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

    @Test
    void shouldRejectSellerAcceptingApplication() throws Exception {
        String buyerToken = registerAndGetToken("buyer-accept-owner", RoleName.BUYER);
        String sellerToken = registerAndGetToken("seller-accept-block", RoleName.SELLER);

        String taskBody = """
                {
                  "title":"Write Unit Tests",
                  "description":"Need full test coverage",
                  "budget":120.00
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
                  "proposedAmount":110.00,
                  "coverLetter":"I can complete this quickly"
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
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectSellerViewingTaskApplicationsList() throws Exception {
        String buyerToken = registerAndGetToken("buyer-visibility", RoleName.BUYER);
        String sellerToken = registerAndGetToken("seller-visibility", RoleName.SELLER);

        String taskBody = """
                {
                  "title":"API Hardening",
                  "description":"Lock down endpoints",
                  "budget":90.00
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
                  "proposedAmount":80.00,
                  "coverLetter":"Applying as seller"
                }
                """.formatted(taskId);

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/applications/task/{taskId}", taskId)
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnSellerOwnApplicationsFromMineEndpoint() throws Exception {
        String buyerToken = registerAndGetToken("buyer-mine", RoleName.BUYER);
        String sellerToken = registerAndGetToken("seller-mine", RoleName.SELLER);

        String taskBody = """
                {
                  "title":"Frontend Polish",
                  "description":"Need responsive cleanup",
                  "budget":140.00
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
                  "proposedAmount":130.00,
                  "coverLetter":"I can do this"
                }
                """.formatted(taskId);

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/applications/mine")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taskId").value(taskId));
    }

    @Test
    void shouldAllowAdminToViewAllApplications() throws Exception {
        String buyerToken = registerAndGetToken("buyer-admin-view", RoleName.BUYER);
        String sellerToken = registerAndGetToken("seller-admin-view", RoleName.SELLER);

        String taskBody = """
                {
                  "title":"Admin Visible Task",
                  "description":"Admin should see all applications",
                  "budget":200.00
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
                  "proposedAmount":180.00,
                  "coverLetter":"Admin list test"
                }
                """.formatted(taskId);

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isCreated());

        String adminToken = ensureAdminAndLogin();

        mockMvc.perform(get("/api/v1/applications/admin/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void shouldRejectNonAdminFromViewingAllApplications() throws Exception {
        String buyerToken = registerAndGetToken("buyer-admin-deny", RoleName.BUYER);

        mockMvc.perform(get("/api/v1/applications/admin/all")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectDuplicateApplicationFromSameSeller() throws Exception {
        String buyerToken = registerAndGetToken("buyer-duplicate", RoleName.BUYER);
        String sellerToken = registerAndGetToken("seller-duplicate", RoleName.SELLER);

        String taskBody = """
                {
                  "title":"Backend API docs",
                  "description":"Need full API documentation",
                  "budget":75.00
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
                  "proposedAmount":70.00,
                  "coverLetter":"I will deliver quickly"
                }
                """.formatted(taskId);

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applyBody))
                .andExpect(status().isBadRequest());
    }

    private String registerAndGetToken(String emailPrefix, RoleName roleName) throws Exception {
        String email = uniqueEmail(emailPrefix);
        String registerBody = """
                {
                  "fullName":"%s",
                  "email":"%s",
                  "password":"password123",
                  "roles":["%s"]
                }
                """.formatted(emailPrefix, email, roleName.name());

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

    private String ensureAdminAndLogin() throws Exception {
        String adminEmail = "admin.integration@test.com";
        String adminPassword = "admin123";

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));

        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseGet(() -> User.builder()
                        .fullName("Integration Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles(new HashSet<>())
                        .build());

        if (adminUser.getRoles() == null) {
            adminUser.setRoles(new HashSet<>());
        }

        adminUser.getRoles().add(adminRole);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        userRepository.save(adminUser);

        String loginBody = """
                {
                  "email":"%s",
                  "password":"%s"
                }
                """.formatted(adminEmail, adminPassword);

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(loginResponse);
        return node.get("token").asText();
    }

    private String uniqueEmail(String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return prefix + "-" + suffix + "@test.com";
    }
}
