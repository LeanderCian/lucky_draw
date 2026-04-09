package com.leander.lottery.admin.controller;

import com.leander.lottery.admin.dto.CreateCampaignRequest;
import com.leander.lottery.admin.service.AuthService;
import com.leander.lottery.admin.service.CampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CampaignControllerCreateCampaignTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampaignService campaignService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateCampaignRequest req;

    private final String adminToken = "admin-token";

    @BeforeEach
    void setUp() {
        req = new CreateCampaignRequest();
        req.setName("test");
        req.setStatus(1);
        req.setMaxTries(10);
        req.setStartTime(1000L);
        req.setEndTime(2000L);
    }

    @Test
    void createSuccess() throws Exception {
        when(campaignService.createCampaign(any())).thenReturn(100L);
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void missingParameters() throws Exception {
        // Missing Token
        mockMvc.perform(post("/api/v1/admin/campaign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        // Missing Name
        CreateCampaignRequest req1 = new CreateCampaignRequest();
        req1.setStatus(1);
        req1.setMaxTries(10);
        req1.setStartTime(1000L);
        req1.setEndTime(2000L);
        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Status
        CreateCampaignRequest req2 = new CreateCampaignRequest();
        req2.setName("test");
        req2.setMaxTries(10);
        req2.setStartTime(1000L);
        req2.setEndTime(2000L);
        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Max Tries
        CreateCampaignRequest req3 = new CreateCampaignRequest();
        req3.setName("test");
        req3.setStatus(1);
        req3.setStartTime(1000L);
        req3.setEndTime(2000L);
        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req3))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Start Time
        CreateCampaignRequest req4 = new CreateCampaignRequest();
        req4.setName("test");
        req4.setStatus(1);
        req4.setMaxTries(10);
        req4.setEndTime(2000L);
        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req4))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Start Time
        CreateCampaignRequest req5 = new CreateCampaignRequest();
        req5.setName("test");
        req5.setStatus(1);
        req5.setMaxTries(10);
        req5.setStartTime(1000L);
        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req5))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidStatus() throws Exception {
        req.setStatus(10);

        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void maxTriesNegative() throws Exception {
        req.setMaxTries(0);

        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        req.setMaxTries(-99);

        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void timeOrderError() throws Exception {
        req.setStartTime(2000L);
        req.setEndTime(1000L);

        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthorized() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/campaign")
                        .header("Authorization", "general-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}