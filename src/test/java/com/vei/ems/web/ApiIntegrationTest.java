package com.vei.ems.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Tests the real REST API end to end (security, validation, database) using an in-memory H2 database. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // ------------------------------------------------------------ helpers

    private String login(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String adminToken() throws Exception {
        return login("admin", "Admin@123");
    }

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    private String fullTimeJson(String email, String phone) {
        return "{\"name\":\"Test Person\",\"email\":\"" + email + "\",\"phone\":\"" + phone + "\","
                + "\"department\":\"ENGINEERING\",\"designation\":\"Developer\",\"joiningDate\":\"2024-01-15\","
                + "\"type\":\"FULL_TIME\",\"basicSalary\":50000}";
    }

    private String createUserAndLogin(String adminToken, String role) throws Exception {
        String username = role.toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 6);
        mvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Secret123\",\"role\":\"" + role + "\"}"))
                .andExpect(status().isCreated());
        return login(username, "Secret123");
    }

    // ------------------------------------------------------------ login and security

    @Test
    void loginWithCorrectPasswordReturnsToken() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employeesNeedALogin() throws Exception {
        mvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
    }

    @Test
    void fakeTokenIsRejected() throws Exception {
        mvc.perform(get("/api/employees").header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------ employees

    @Test
    void adminCanCreateAndListAnEmployee() throws Exception {
        String token = adminToken();
        String email = uniqueEmail();

        mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullTimeJson(email, "9876543210")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").isNotEmpty())
                .andExpect(jsonPath("$.monthlyPay").value(59000.0));

        mvc.perform(get("/api/employees").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='" + email + "')]").exists());
    }

    @Test
    void partTimePayIsRateTimesHours() throws Exception {
        String token = adminToken();
        String json = "{\"name\":\"Part Timer\",\"email\":\"" + uniqueEmail() + "\",\"phone\":\"8765432109\","
                + "\"department\":\"MARKETING\",\"designation\":\"Writer\",\"joiningDate\":\"2024-06-17\","
                + "\"type\":\"PART_TIME\",\"hourlyRate\":300,\"hoursWorked\":80}";

        mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.monthlyPay").value(24000.0));
    }

    @Test
    void invalidPhoneIsRejectedWithFieldError() throws Exception {
        mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullTimeJson(uniqueEmail(), "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        String token = adminToken();
        String email = uniqueEmail();

        mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullTimeJson(email, "9876543210")))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullTimeJson(email, "9876543211")))
                .andExpect(status().isConflict());
    }

    @Test
    void deletedEmployeeIsGone() throws Exception {
        String token = adminToken();
        MvcResult created = mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullTimeJson(uniqueEmail(), "9876543210")))
                .andExpect(status().isCreated())
                .andReturn();
        long id = mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(delete("/api/employees/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/employees/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------ roles

    @Test
    void viewerCanReadButNotCreate() throws Exception {
        String viewer = createUserAndLogin(adminToken(), "VIEWER");

        mvc.perform(get("/api/employees").header("Authorization", "Bearer " + viewer))
                .andExpect(status().isOk());

        mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + viewer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullTimeJson(uniqueEmail(), "9876543210")))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrCanCreateButCannotManageUsers() throws Exception {
        String hr = createUserAndLogin(adminToken(), "HR");

        mvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + hr)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullTimeJson(uniqueEmail(), "9876543210")))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/users").header("Authorization", "Bearer " + hr))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanSeeTheActivityLog() throws Exception {
        mvc.perform(get("/api/audit").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void reportSummaryWorks() throws Exception {
        mvc.perform(get("/api/reports/summary").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees").exists());
    }

    @Test
    void reportCanBeDownloadedAsCsv() throws Exception {
        mvc.perform(get("/api/reports/export").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("employee-report.csv")))
                .andExpect(content().string(containsString("EMPLOYEE MANAGEMENT SYSTEM - REPORT")));
    }

    @Test
    void reportDownloadNeedsALogin() throws Exception {
        mvc.perform(get("/api/reports/export")).andExpect(status().isUnauthorized());
    }
}
