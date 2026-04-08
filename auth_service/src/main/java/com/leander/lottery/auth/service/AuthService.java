package com.leander.lottery.auth.service;

import com.leander.lottery.auth.dto.RegisterRequest;
import com.leander.lottery.auth.exception.DuplicateUserException;

public interface AuthService {
    // 登入驗證，成功回傳 Token，失敗回傳 null
    String authenticate(String username, String password);

    // 註冊使用者
    Long registerUser(RegisterRequest regRequest) throws DuplicateUserException;

    // 驗證 Token 是否有效
    boolean isTokenValid(String token);

    // 檢查 Token 擁有者是否為管理員
    boolean isAdmin(String token);
}
