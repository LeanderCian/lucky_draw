package com.leander.lottery.auth.model.enums;

public enum UserRole {
    GENERAL_USER(1),
    ADMIN(2);

    private final int value;

    UserRole(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    // 根據數字尋找對應的 Enum
    public static UserRole fromValue(Integer value) {
        if (value == null) return GENERAL_USER; // 預設值
        for (UserRole role : UserRole.values()) {
            if (role.value == value) {
                return role;
            }
        }
        return null; // 找不到則回傳 null
    }
}