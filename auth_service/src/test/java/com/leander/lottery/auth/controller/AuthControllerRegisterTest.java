package com.leander.lottery.auth.controller;

import com.leander.lottery.auth.dto.RegisterRequest;
import com.leander.lottery.auth.exception.DuplicateUserException;
import com.leander.lottery.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerRegisterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper; // 用於將物件轉為 JSON

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setName("test");
        validRequest.setPassword("password123");
        validRequest.setEmail("test@test.com");
    }

    @Test
    void registerSuccess() throws Exception {
        when(authService.registerUser(any())).thenReturn(100L);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void registerMissingParameters() throws Exception {
        // Missing Name
        RegisterRequest req1 = new RegisterRequest();
        req1.setPassword("password123");
        req1.setEmail("test@test.com");
        mockMvc.perform(post("/api/v1/auth/register")
                        .content(objectMapper.writeValueAsString(req1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Password
        RegisterRequest req2 = new RegisterRequest();
        req2.setName("test");
        req2.setEmail("test@test.com");
        mockMvc.perform(post("/api/v1/auth/register")
                        .content(objectMapper.writeValueAsString(req2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Email
        RegisterRequest req3 = new RegisterRequest();
        req3.setName("test");
        req3.setPassword("password123");
        mockMvc.perform(post("/api/v1/auth/register")
                        .content(objectMapper.writeValueAsString(req3))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerInvalidEmail() throws Exception {
        validRequest.setEmail("invalid-email-format");
        mockMvc.perform(post("/api/v1/auth/register")
                        .content(objectMapper.writeValueAsString(validRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerNonExistentRole() throws Exception {
        validRequest.setRole(99);
        when(authService.registerUser(any())).thenReturn(100L);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUnauthorized() throws Exception {
        validRequest.setRole(2);
        when(authService.isTokenValid(anyString())).thenReturn(false);
        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "expired-token")
                        .content(objectMapper.writeValueAsString(validRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerForbidden() throws Exception {
        validRequest.setRole(2);
        when(authService.isTokenValid(anyString())).thenReturn(true);
        when(authService.isAdmin(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/register")
                        .header("Authorization", "general-user-token")
                        .content(objectMapper.writeValueAsString(validRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerDuplicateData() throws Exception {
        doThrow(new DuplicateUserException("Duplicate username or email"))
                .when(authService).registerUser(any());

        mockMvc.perform(post("/api/v1/auth/register")
                        .content(objectMapper.writeValueAsString(validRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("Duplicate username or email"));
    }
}