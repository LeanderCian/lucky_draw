package com.leander.lottery.admin.controller;

import com.leander.lottery.admin.dto.*;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.service.AuthService;
import com.leander.lottery.admin.service.CampaignService;
import com.leander.lottery.admin.service.LotteryCountService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/campaign")
public class CampaignController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private LotteryCountService lotteryCountService;

    // 建立活動
    @PostMapping
    public ResponseEntity<?> createCampaign(
            @RequestHeader(value = "Authorization") String token,
            @Valid @RequestBody CreateCampaignRequest req) {
        // 結束時間必須大於開始時間
        if (req.getEndTime() <= req.getStartTime()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("start_time must before end_time");
        }

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
            Long campaignId = campaignService.createCampaign(req);
            return ResponseEntity.ok(Map.of("id", campaignId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred. Please contact the system administrator.");
        }
    }

    // 修改活動
    @PutMapping("/{campaign_id}")
    public ResponseEntity<?> updateCampaign(
            @RequestHeader(value = "Authorization") String token,
            @PathVariable("campaign_id") Long campaignId,
            @Valid @RequestBody UpdateCampaignRequest req) {
        // 結束時間必須大於開始時間
        if (req.getEndTime() <= req.getStartTime()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("start_time must before end_time");
        }

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
            campaignService.updateCampaign(campaignId, req);
            return ResponseEntity.ok().build();
        } catch(ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred. Please contact the system administrator.");
        }
    }

    // 查詢活動
    @GetMapping("/{campaign_id}")
    public ResponseEntity<?> getCampaign(
            @RequestHeader("Authorization") String token,
            @PathVariable("campaign_id") Long campaignId) {

        // 檢查 Token 有效性
        if (!authService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non-existent token or token out of date");
        }

        // 檢查是否有管理員權限
        if (!authService.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("not admin role");
        }

        // 取得活動資料
        try {
            CampaignResponse response = campaignService.getCampaignById(campaignId);
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

    // 設定使用者抽獎次數
    @PutMapping("/{campaign_id}/user/{user_id}")
    public ResponseEntity<?> setLotteryCount(
            @RequestHeader(value = "Authorization") String token,
            @PathVariable("campaign_id") Long campaignId,
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody SetLotteryCountRequest req) {
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
            lotteryCountService.updateLotteryCount(campaignId, userId, req.getTotalLotteryCount());
            return ResponseEntity.ok().build();
        } catch(ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred. Please contact the system administrator.");
        }
    }

    // 取得使用者抽獎次數
    @GetMapping("/{campaign_id}/user")
    public ResponseEntity<?> setLotteryCount(
            @RequestHeader(value = "Authorization") String token,
            @PathVariable("campaign_id") Long campaignId) {
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
            LotteryCountResponse res = lotteryCountService.getLotteryCount(campaignId);
            return ResponseEntity.ok(res);
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