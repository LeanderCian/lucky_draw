package com.leander.lottery.admin.controller;

import com.leander.lottery.admin.dto.ItemResponse;
import com.leander.lottery.admin.entity.Item;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.service.AuthService;
import com.leander.lottery.admin.service.ItemService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerGetItemTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String adminToken = "admin-token";

    @Test
    void getSuccess() throws Exception {
        Item i = new Item();
        i.setId(111L);
        i.setCampaignId(222L);
        i.setName("test");
        i.setProbability(100000000);
        i.setTotalStock(1000L);
        i.setCurrentStock(555L);
        ItemResponse res = new ItemResponse(i);

        when(itemService.getItemById(any())).thenReturn(res);
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/item/111")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(111L))
                .andExpect(jsonPath("$.campaign_id").value(222L))
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.probability").value(100000000))
                .andExpect(jsonPath("$.total_stock").value(1000L))
                .andExpect(jsonPath("$.current_stock").value(555L));
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
        when(itemService.getItemById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("no this item"));
        when(authService.isTokenValid(any())).thenReturn(true);
        when(authService.isAdmin(any())).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/item/"+nonExistentId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }
}