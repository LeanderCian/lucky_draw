package com.leander.lottery.lottery.controller;

import com.leander.lottery.lottery.dto.SetLotteryCountRequest;
import com.leander.lottery.lottery.exception.ResourceNotFoundException;
import com.leander.lottery.lottery.service.AuthService;
import com.leander.lottery.lottery.service.LotteryCountService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CampaignControllerSetLotteryCountTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LotteryCountService lotteryCountService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private SetLotteryCountRequest req;

    private final String adminToken = "admin-token";

    @BeforeEach
    void setUp() {
        req = new SetLotteryCountRequest();
        req.setTotalLotteryCount(1000);
    }

    @Test
    void success() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(put("/api/v1/admin/campaign/123/user/456")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void missingParameters() throws Exception {
        // Missing Token
        mockMvc.perform(put("/api/v1/admin/campaign/123/user/456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TotalLotteryCountNegative() throws Exception {
        req.setTotalLotteryCount(-1);

        mockMvc.perform(put("/api/v1/admin/campaign/123/user/456")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthorized() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(false);

        mockMvc.perform(put("/api/v1/admin/campaign/123/user/456")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(put("/api/v1/admin/campaign/123/user/456")
                        .header("Authorization", "general-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void campaignNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("No this campaign")).when(lotteryCountService).updateLotteryCount(any(), any(), any());
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(put("/api/v1/admin/campaign/123/user/456")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("No this user")).when(lotteryCountService).updateLotteryCount(any(), any(), any());
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(put("/api/v1/admin/campaign/123/user/456")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}