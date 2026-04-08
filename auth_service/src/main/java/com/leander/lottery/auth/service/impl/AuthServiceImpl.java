package com.leander.lottery.auth.service.impl;

import com.leander.lottery.auth.dto.*;
import com.leander.lottery.auth.entity.User;
import com.leander.lottery.auth.exception.DuplicateUserException;
import com.leander.lottery.auth.model.enums.*;
import com.leander.lottery.auth.repository.UserRepository;
import com.leander.lottery.auth.service.AuthService;
import com.leander.lottery.auth.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils; // 假設你有一個處理 JWT 的工具類

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(readOnly = true)
    public String authenticate(String name, String password) {
        // 1. 找尋使用者
        return userRepository.findByName(name)
                .filter(user -> passwordEncoder.matches(password, user.getPassword())) // 2. 比對加密密碼
                .map(user -> jwtUtils.generateToken(user.getId(), user.getRole())) // 3. 生成 Token
                .orElse(null);
    }

    @Override
    @Transactional
    public Long registerUser(RegisterRequest regRequest) throws DuplicateUserException {
        // 1. 檢查帳號或 Email 是否重複
        if (userRepository.existsByName(regRequest.getName()) ||
                userRepository.existsByEmail(regRequest.getEmail())) {
            throw new DuplicateUserException("Username or Email already exists");
        }

        // 2. 建立新使用者並加密密碼
        User newUser = new User();
        newUser.setName(regRequest.getName());
        newUser.setEmail(regRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(regRequest.getPassword())); // 密碼加密
        newUser.setRole(regRequest.getRole());

        // 3. 存入資料庫
        User u = userRepository.save(newUser);

        // 4. 回傳user id
        return u.getId();
    }

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
