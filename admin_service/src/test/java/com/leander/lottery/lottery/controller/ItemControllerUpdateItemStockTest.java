package com.leander.lottery.lottery.controller;

import com.leander.lottery.lottery.dto.UpdateItemStockRequest;
import com.leander.lottery.lottery.exception.ResourceNotFoundException;
import com.leander.lottery.lottery.exception.StockNotEnoughException;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerUpdateItemStockTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UpdateItemStockRequest req;

    private final String adminToken = "admin-token";

    @BeforeEach
    void setUp() {
        req = new UpdateItemStockRequest();
        req.setIncrementAmount(100L);
    }

    @Test
    void updateSuccess() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(patch("/api/v1/admin/item/123")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(itemService, times(1)).updateItemStock(any(), any());
    }

    @Test
    void missingParameters() throws Exception {
        // Missing Token
        mockMvc.perform(patch("/api/v1/admin/item/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());

        // Missing increment_amount
        UpdateItemStockRequest req1 = new UpdateItemStockRequest();
        mockMvc.perform(patch("/api/v1/admin/item/123")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void incrementAmountBeZero() throws Exception {
        req.setIncrementAmount(0L);

        mockMvc.perform(patch("/api/v1/admin/item/123")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stockNotEnough() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);
        when(itemService.updateItemStock(any(), any()))
                .thenThrow(new StockNotEnoughException("item stock is not enough"));

        mockMvc.perform(patch("/api/v1/admin/item/123")
                        .header("Authorization", adminToken)
                        .content(objectMapper.writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void unauthorized() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(false);

        mockMvc.perform(patch("/api/v1/admin/item/123")
                        .header("Authorization", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidden() throws Exception {
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(false);

        mockMvc.perform(patch("/api/v1/admin/item/123")
                        .header("Authorization", "general-user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void itemNotFound() throws Exception {
        when(itemService.updateItemStock(any(), any()))
                .thenThrow(new ResourceNotFoundException("no this item"));
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(patch("/api/v1/admin/item/123")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}