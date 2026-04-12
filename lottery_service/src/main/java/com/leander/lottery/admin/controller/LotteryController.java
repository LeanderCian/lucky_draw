package com.leander.lottery.admin.controller;

import com.leander.lottery.admin.dto.*;
import com.leander.lottery.admin.exception.ExceedMaxTriesException;
import com.leander.lottery.admin.exception.RemainingCountNotEnoughException;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.service.AuthService;
import com.leander.lottery.admin.service.LotteryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/lottery")
public class LotteryController {

    @Autowired
    private AuthService authService;

    @Autowired
    private LotteryService lotteryService;

    // 抽獎
    @PostMapping("/draw")
    public ResponseEntity<?> createCampaign(
            @RequestHeader(value = "Authorization") String token,
            @Valid @RequestBody DrawRequest req) {
        // 檢查 Token 有效性
        if (!authService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non-existent token or token out of date");
        }

        // 檢查是否有管理員權限
        if (!authService.isGeneralUser(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("not general user role");
        }

        Long userId = authService.getUserId(token);

        // 執行業務邏輯
        try {
            DrawResponse response = lotteryService.executeDraw(userId, req);
            return ResponseEntity.ok(response);
        } catch(ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch(ExceedMaxTriesException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch(RemainingCountNotEnoughException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred. Please contact the system administrator.");
        }
    }
}