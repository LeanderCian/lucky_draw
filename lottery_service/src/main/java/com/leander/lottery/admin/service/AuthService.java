package com.leander.lottery.admin.service;

public interface AuthService {
    // 驗證 Token 是否有效
    boolean isTokenValid(String token);

    // 檢查 Token 擁有者是否為管理員
    boolean isAdmin(String token);

    // 檢查 Token 擁有者是否為一般使用者
    boolean isGeneralUser(String token);

    // 取得使用者id
    Long getUserId(String token);
}
