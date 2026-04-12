package com.leander.lottery.admin.controller;

import com.leander.lottery.admin.dto.UpdateCampaignRequest;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CampaignControllerUpdateCampaignTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampaignService campaignService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UpdateCampaignRequest req;

    private final String adminToken = "admin-token";

    @BeforeEach
    void setUp() {
        req = new UpdateCampaignRequest();
        req.setName("test");
        req.setMaxTries(10);
        req.setStartTime(1000L);
        req.setEndTime(2000L);
    }

    @Test
    void updateSuccess() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(campaignService, times(1)).updateCampaign(any(), any());
    }

    @Test
    void missingParameters() throws Exception {
        // Missing Token
        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        // Missing Name
        UpdateCampaignRequest req1 = new UpdateCampaignRequest();
        req1.setMaxTries(10);
        req1.setStartTime(1000L);
        req1.setEndTime(2000L);
        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Max Tries
        UpdateCampaignRequest req2 = new UpdateCampaignRequest();
        req2.setName("test");
        req2.setStartTime(1000L);
        req2.setEndTime(2000L);
        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Start Time
        UpdateCampaignRequest req3 = new UpdateCampaignRequest();
        req3.setName("test");
        req3.setMaxTries(10);
        req3.setEndTime(2000L);
        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req3))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing End Time
        UpdateCampaignRequest req4 = new UpdateCampaignRequest();
        req4.setName("test");
        req4.setMaxTries(10);
        req4.setStartTime(1000L);
        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req4))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void maxTriesNegative() throws Exception {
        req.setMaxTries(0);

        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        req.setMaxTries(-99);

        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void timeOrderError() throws Exception {
        req.setStartTime(2000L);
        req.setEndTime(1000L);

        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthorized() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(false);

        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(put("/api/v1/admin/campaign/123")
                        .header("Authorization", "general-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void campaignNotFound() throws Exception {
        Long nonExistentId = 999L;
        when(campaignService.getCampaignById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("no this item"));
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/item/"+nonExistentId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }
}