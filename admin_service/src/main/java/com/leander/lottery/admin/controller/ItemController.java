package com.leander.lottery.admin.controller;

import com.leander.lottery.admin.dto.CreateItemRequest;
import com.leander.lottery.admin.dto.ItemResponse;
import com.leander.lottery.admin.exception.ProbabilityExceededException;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.service.AuthService;
import com.leander.lottery.admin.service.ItemService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/item")
public class ItemController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ItemService itemService;

    // 建立活動
    @PostMapping
    public ResponseEntity<?> createItem(
            @RequestHeader(value = "Authorization") String token,
            @Valid @RequestBody CreateItemRequest req) {
        // 檢查 Token 有效性
        if (!authService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non-existent token or token out of date");
        }

        // 檢查是否有管理員權限
        if (!authService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("not admin role");
        }

        // 執行業務邏輯
        try {
            Long itemId = itemService.createItem(req);
            return ResponseEntity.ok(Map.of("id", itemId));
        } catch(ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch(ProbabilityExceededException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred. Please contact the system administrator.");
        }
    }

    // 查詢獎品
    @GetMapping("/{item_id}")
    public ResponseEntity<?> getItem(
            @RequestHeader("Authorization") String token,
            @PathVariable("item_id") Long itemId) {

        // 檢查 Token 有效性
        if (!authService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non-existent token or token out of date");
        }

        // 檢查是否有管理員權限
        if (!authService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("not admin role");
        }

        // 取得獎品資料
        try {
            ItemResponse response = itemService.getItemById(itemId);
            return ResponseEntity.ok(response);
        } catch(ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred. Please contact the system administrator.");
        }
    }
}