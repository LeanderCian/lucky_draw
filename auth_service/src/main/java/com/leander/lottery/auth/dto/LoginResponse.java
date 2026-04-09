package com.leander.lottery.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String Authorization;

    public LoginResponse(String authorization) {
        this.Authorization = authorization;
    }
}