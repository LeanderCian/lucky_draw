package com.leander.lottery.admin.controller;

import com.leander.lottery.admin.dto.LotteryCountResponse;
import com.leander.lottery.admin.entity.LotteryCount;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.service.AuthService;
import com.leander.lottery.admin.service.LotteryCountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CampaignControllerGetLotteryCountTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LotteryCountService lotteryCountService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String adminToken = "admin-token";

    @Test
    void getSuccess() throws Exception {
        Long campaignId = 123L;

        LotteryCountResponse.UserCount u1 = new LotteryCountResponse.UserCount(111L, 11111, 1111);
        LotteryCountResponse.UserCount u2 = new LotteryCountResponse.UserCount(222L, 22222, 2222);
        ArrayList<LotteryCountResponse.UserCount> userCounts = new ArrayList<LotteryCountResponse.UserCount>();
        userCounts.add(u1);
        userCounts.add(u2);

        LotteryCountResponse res = new LotteryCountResponse(campaignId, userCounts);

        when(lotteryCountService.getLotteryCount(any(Long.class))).thenReturn(res);
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/campaign/123/user")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignId").value(123L))
                .andExpect(jsonPath("$.users.size()").value(2))
                .andExpect(jsonPath("$.users[0].id").value(111L))
                .andExpect(jsonPath("$.users[0].total_lottery_count").value("11111"))
                .andExpect(jsonPath("$.users[0].remaining_lottery_count").value(1111))
                .andExpect(jsonPath("$.users[1].id").value(222L))
                .andExpect(jsonPath("$.users[1].total_lottery_count").value("22222"))
                .andExpect(jsonPath("$.users[1].remaining_lottery_count").value(2222));
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

        mockMvc.perform(get("/api/v1/admin/campaign/123/user")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(get("/api/v1/admin/campaign/123/user")
                        .header("Authorization", "general-user-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void campaignNotFound() throws Exception {
        Long nonExistentId = 999L;
        when(lotteryCountService.getLotteryCount(nonExistentId))
                .thenThrow(new ResourceNotFoundException("no this campaign"));
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/campaign/999/user")
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }
}