package com.leander.lottery.lottery.controller;

import com.leander.lottery.lottery.dto.CreateItemRequest;
import com.leander.lottery.lottery.exception.ProbabilityExceededException;
import com.leander.lottery.lottery.exception.ResourceNotFoundException;
import com.leander.lottery.lottery.service.AuthService;
import com.leander.lottery.lottery.service.ItemService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerCreateItemTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateItemRequest req;

    private final String adminToken = "admin-token";

    @BeforeEach
    void setUp() {
        req = new CreateItemRequest();
        req.setCampaignId(111L);
        req.setName("test");
        req.setProbability(10000000);
        req.setTotalStock(10L);
    }

    @Test
    void createSuccess() throws Exception {
        when(itemService.createItem(any())).thenReturn(100L);
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void missingParameters() throws Exception {
        // Missing Token
        mockMvc.perform(post("/api/v1/admin/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        // Missing Campaign Id
        CreateItemRequest req1 = new CreateItemRequest();
        req1.setName("test");
        req1.setProbability(10000000);
        req1.setTotalStock(10L);
        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Name
        CreateItemRequest req2 = new CreateItemRequest();
        req2.setCampaignId(111L);
        req2.setProbability(10000000);
        req2.setTotalStock(10L);
        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Probability
        CreateItemRequest req3 = new CreateItemRequest();
        req3.setCampaignId(111L);
        req3.setName("test");
        req3.setTotalStock(10L);
        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req3))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Missing Total Stock
        CreateItemRequest req4 = new CreateItemRequest();
        req4.setCampaignId(111L);
        req4.setName("test");
        req4.setProbability(10000000);
        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req4))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void probabilityLessThanZero() throws Exception {
        req.setProbability(-1);

        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void probabilityOverHundred() throws Exception {
        req.setProbability(90000000);

        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);
        when(itemService.createItem(any()))
                .thenThrow(new ProbabilityExceededException("probability exceeded"));

        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void totalStockNegative() throws Exception {
        req.setTotalStock(-1L);

        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthorized() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", "general-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void campaignNotFound() throws Exception {
        when(itemService.createItem(any()))
                .thenThrow(new ResourceNotFoundException("no this campaign"));
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/item")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}