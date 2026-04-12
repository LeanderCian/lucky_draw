package com.leander.lottery.lottery.controller;

import com.leander.lottery.lottery.dto.CampaignResponse;
import com.leander.lottery.lottery.entity.Campaign;
import com.leander.lottery.lottery.exception.ResourceNotFoundException;
import com.leander.lottery.lottery.service.AuthService;
import com.leander.lottery.lottery.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CampaignControllerGetCampaignTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampaignService campaignService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String adminToken = "admin-token";

    @Test
    void getSuccess() throws Exception {
        Campaign c = new Campaign();
        c.setId(111L);
        c.setName("ca1111");
        c.setMaxTries(11);
        c.setStartTime(1000L);
        c.setEndTime(2000L);
        CampaignResponse res = new CampaignResponse(c);

        when(campaignService.getCampaignById(any())).thenReturn(res);
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/campaign/111")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(111L))
                .andExpect(jsonPath("$.name").value("ca1111"))
                .andExpect(jsonPath("$.max_tries").value(11))
                .andExpect(jsonPath("$.start_time").value(1000L))
                .andExpect(jsonPath("$.end_time").value(2000L));
    }

    @Test
    void missingToken() throws Exception {
        // Missing Token
        mockMvc.perform(get("/api/v1/admin/campaign/111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthorized() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(false);

        mockMvc.perform(get("/api/v1/admin/campaign/111")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(get("/api/v1/admin/campaign/111")
                        .header("Authorization", "general-user-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void campaignNotFound() throws Exception {
        Long nonExistentId = 999L;
        when(campaignService.getCampaignById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("no this campaign"));
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/campaign/"+nonExistentId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }
}