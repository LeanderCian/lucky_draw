package com.leander.lottery.lottery.service;

public interface AuthService {
    // 驗證 Token 是否有效
    boolean isTokenValid(String token);

    // 檢查 Token 擁有者是否為管理員
    boolean isAdmin(String token);
}
