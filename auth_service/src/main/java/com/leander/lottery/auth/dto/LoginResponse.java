package com.leander.lottery.auth.dto;

public class LoginResponse {
    private String Authorization;

    public LoginResponse(String authorization) {
        this.Authorization = authorization;
    }

    // Getter and Setter
    public String getAuthorization() { return Authorization; }
    public void setAuthorization(String authorization) { this.Authorization = authorization; }
}