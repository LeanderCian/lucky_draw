package com.leander.lottery.auth.controller;

import com.leander.lottery.auth.dto.*;
import com.leander.lottery.auth.exception.DuplicateUserException;
import com.leander.lottery.auth.model.enums.*;
import com.leander.lottery.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 註冊
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody RegisterRequest req) {
        // 檢查 Role 是否存在
        UserRole targetRole = UserRole.fromValue(req.getRole());
        if (targetRole == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Non-existent role");
        }

        // 權限檢查：如果要創建管理員 (Role 2)
        if (targetRole == UserRole.ADMIN) {
            if (!StringUtils.hasText(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token required for admin creation");
            }

            // 檢查 Token 有效性與是否具備管理員權限
            if (!authService.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non-existent token or token out of date");
            }

            if (!authService.isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Create admin without admin token");
            }
        }

        // 執行註冊邏輯
        try {
            authService.registerUser(req);
            return ResponseEntity.ok().build();
        } catch (DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate username or email");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred. Please contact the system administrator.");
        }
    }

    // 登入
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        // 呼叫 Service 進行驗證並取得 Token
        String token = authService.authenticate(
                req.getName(),
                req.getPassword()
        );

        if (token != null) {
            // 登入成功：回傳 200 與 Token
            return ResponseEntity.ok(new LoginResponse(token));
        } else {
            // 登入失敗：回傳 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}