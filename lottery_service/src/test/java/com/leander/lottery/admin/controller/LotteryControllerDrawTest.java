package com.leander.lottery.admin.controller;

import com.leander.lottery.admin.dto.DrawRequest;
import com.leander.lottery.admin.dto.DrawResponse;
import com.leander.lottery.admin.dto.DrawResponse.DrawResult;
import com.leander.lottery.admin.exception.ExceedMaxTriesException;
import com.leander.lottery.admin.exception.RemainingCountNotEnoughException;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.service.AuthService;
import com.leander.lottery.admin.service.LotteryService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LotteryControllerDrawTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private DrawRequest req;

    private final String generalUserToken = "general-user-token";

    @MockitoBean
    private LotteryService lotteryService;

    @BeforeEach
    void setUp() {
        req = new DrawRequest();
        req.setCampaignId(111L);
        req.setCount(10);
    }

    @Test
    void drawSuccess() throws Exception {
        DrawResult result1 = new DrawResult();
        result1.setDrawId("1");
        result1.setItemId(1L);
        result1.setItemName("item1");
        result1.setWin(true);

        DrawResult result2 = new DrawResult();
        result2.setDrawId("2");
        result2.setItemId(2L);
        result2.setItemName("item2");
        result2.setWin(true);

        DrawResult result3 = new DrawResult();
        result3.setDrawId("3");
        result3.setItemName("銘謝惠顧");
        result3.setWin(false);

        ArrayList<DrawResult> results = new ArrayList<>();
        results.add(result1);
        results.add(result2);
        results.add(result3);

        DrawResponse res = new DrawResponse(results);

        when(lotteryService.executeDraw(any(), any())).thenReturn(res);
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isGeneralUser(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/lottery/draw")
                        .header("Authorization", generalUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].draw_id").value(result1.getDrawId()))
                .andExpect(jsonPath("$.results[0].item_id").value(result1.getItemId()))
                .andExpect(jsonPath("$.results[0].item_name").value(result1.getItemName()))
                .andExpect(jsonPath("$.results[0].is_win").value(result1.isWin()))
                .andExpect(jsonPath("$.results[1].draw_id").value(result2.getDrawId()))
                .andExpect(jsonPath("$.results[1].item_id").value(result2.getItemId()))
                .andExpect(jsonPath("$.results[1].item_name").value(result2.getItemName()))
                .andExpect(jsonPath("$.results[1].is_win").value(result2.isWin()))
                .andExpect(jsonPath("$.results[2].draw_id").value(result3.getDrawId()))
                .andExpect(jsonPath("$.results[2].item_id").value(result3.getItemId()))
                .andExpect(jsonPath("$.results[2].item_name").value(result3.getItemName()))
                .andExpect(jsonPath("$.results[2].is_win").value(result3.isWin()));
    }

    @Test
    void missingParameters() throws Exception {
        // Missing Token
        mockMvc.perform(post("/api/v1/lottery/draw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        // Missing Campaign ID
        DrawRequest req1 = new DrawRequest();
        req1.setCount(10);
        mockMvc.perform(post("/api/v1/lottery/draw")
                        .header("Authorization", generalUserToken)
                        .content(objectMapper.writeValueAsString(req1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Count
        DrawRequest req2 = new DrawRequest();
        req2.setCampaignId(111L);
        mockMvc.perform(post("/api/v1/lottery/draw")
                        .header("Authorization", generalUserToken)
                        .content(objectMapper.writeValueAsString(req2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void countNegative() throws Exception {
        req.setCount(0);

        mockMvc.perform(post("/api/v1/lottery/draw")
                        .header("Authorization", generalUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void countExceeded() throws Exception {
        when(lotteryService.executeDraw(any(), any()))
                .thenThrow(new ExceedMaxTriesException("Exceed max tries"));
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isGeneralUser(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/lottery/draw")
                        .header("Authorization", generalUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void remainingCountNotEnough() throws Exception {
        when(lotteryService.executeDraw(any(), any()))
                .thenThrow(new RemainingCountNotEnoughException("Remaining is not enough"));
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isGeneralUser(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/lottery/draw")
                        .header("Authorization", generalUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthorized() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/lottery/draw")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isGeneralUser(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/lottery/draw")
                        .header("Authorization", "admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void campaignNotFound() throws Exception {
        when(lotteryService.executeDraw(any(), any()))
                .thenThrow(new ResourceNotFoundException("no this campaign"));
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isGeneralUser(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/lottery/draw")
                        .header("Authorization", generalUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}