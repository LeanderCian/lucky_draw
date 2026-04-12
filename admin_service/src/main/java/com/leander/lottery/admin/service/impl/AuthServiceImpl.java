package com.leander.lottery.admin.service.impl;

import com.leander.lottery.admin.model.enums.*;
import com.leander.lottery.admin.service.AuthService;
import com.leander.lottery.admin.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtUtils jwtUtils; // 假設你有一個處理 JWT 的工具類

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean isTokenValid(String token) {
        try {
            return jwtUtils.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isAdmin(String token) {
        Integer roleValue = jwtUtils.getRoleFromToken(token);
        return UserRole.ADMIN.getValue() == roleValue;
    }
}
