package com.leander.lottery.admin.controller;

import com.leander.lottery.admin.dto.*;
import com.leander.lottery.admin.exception.ResourceNotFoundException;
import com.leander.lottery.admin.model.enums.*;
import com.leander.lottery.admin.service.AuthService;
import com.leander.lottery.admin.service.CampaignService;
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

    // 建立活動
    @PostMapping
    public ResponseEntity<?> createCampagin(
            @RequestHeader(value = "Authorization") String token,
            @Valid @RequestBody CreateCampaignRequest req) {
        // 狀態是否有定義
        CampaignStatus campaignStatus = CampaignStatus.fromValue(req.getStatus());
        if (campaignStatus == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("campaign status not defined");
        }

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred. Please contact the system administrator.");
        }
    }
}